/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.timeseries.impl.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.timeseries.Aggregation;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.TagMatcher;
import org.opennms.integration.api.v1.timeseries.TimeSeriesFetchRequest;
import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * Simulates a time series storage in memory (Guava cache). The implementation is super simple and not very efficient.
 * For testing and evaluating purposes only, not for production.
 */
public class InMemoryStorage implements TimeSeriesStorage {

    private final ConcurrentMap<Metric, Collection<Sample>> data;

    private final MetricRegistry metrics = new MetricRegistry();
    private final Meter samplesWritten = metrics.meter("samplesWritten");

    public InMemoryStorage () {
        data = new ConcurrentHashMap<>();
    }

    @Override
    public void store(final List<Sample> samples) {
        Objects.requireNonNull(samples);
        for(Sample sample : samples) {
            Collection<Sample> timeseries = data.computeIfAbsent(sample.getMetric(), k -> new ConcurrentLinkedQueue<>());
            timeseries.add(sample);
        }
        samplesWritten.mark(samples.size());
    }

    @Override
    public List<Metric> findMetrics(Collection<TagMatcher> tagMatchers) {
        Objects.requireNonNull(tagMatchers);
        if(tagMatchers.isEmpty()) {
            throw new IllegalArgumentException("We expect at least one TagMatcher but none was given.");
        }
        return data.keySet()
                .stream()
                .filter(metric -> this.matches(tagMatchers, metric))
                .collect(Collectors.toList());
    }

    public Set<Metric> getAllMetrics() {
        return data.keySet();
    }

    /** Each matcher must be matched by at least one tag. */
    private boolean matches(final Collection<TagMatcher> matchers, final Metric metric) {
        final Set<Tag> searchableTags = new HashSet<>(metric.getIntrinsicTags());
        searchableTags.addAll(metric.getMetaTags());

        for(TagMatcher matcher : matchers) {
            if(searchableTags.stream().noneMatch(t -> this.matches(matcher, t))) {
                return false; // this TagMatcher didn't find any matching tag => this Metric is not part of search result;
            }
        }
        return true; // all matched
    }

    private boolean matches(final TagMatcher matcher, final Tag tag) {

        if(!matcher.getKey().equals(tag.getKey())) {
            return false; // not even the key matches => we are done.
        }

        // Tags have always a non null value so we don't have to null check for them.
        if(TagMatcher.Type.EQUALS == matcher.getType()) {
            return tag.getValue().equals(matcher.getValue());
        } else if (TagMatcher.Type.NOT_EQUALS == matcher.getType()) {
            return !tag.getValue().equals(matcher.getValue());
        } else if (TagMatcher.Type.EQUALS_REGEX == matcher.getType()) {
            return tag.getValue().matches(matcher.getValue());
        } else if (TagMatcher.Type.NOT_EQUALS_REGEX == matcher.getType()) {
            return !tag.getValue().matches(matcher.getValue());
        } else {
            throw new IllegalArgumentException("Implement me for " + matcher.getType());
        }
    }

    @Override
    public List<Sample> getTimeseries(TimeSeriesFetchRequest request) {
        Objects.requireNonNull(request);
        if(request.getAggregation() != Aggregation.NONE) {
            throw new IllegalArgumentException(String.format("Aggregation %s is not supported.", request.getAggregation()));
        }

        if(!data.containsKey(request.getMetric())){
            return Collections.emptyList();
        }
        return data.get(request.getMetric()).stream()
                .filter(sample -> sample.getTime().isAfter(request.getStart()))
                .filter(sample -> sample.getTime().isBefore(request.getEnd()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Metric metric) {
        Objects.requireNonNull(metric);
        this.data.remove(metric);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }
}
