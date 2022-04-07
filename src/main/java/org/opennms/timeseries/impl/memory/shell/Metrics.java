/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2022 The OpenNMS Group, Inc.
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

package org.opennms.timeseries.impl.memory.shell;


import java.util.Comparator;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.timeseries.impl.memory.InMemoryStorage;

@Command(scope = "opennms-tss-inmemory", name = "metrics", description = "Display all metrics.")
@Service
public class Metrics implements Action {

    @Reference
    private InMemoryStorage tss;

    @Override
    public Object execute() {
        tss.getAllMetrics()
            .stream()
            .sorted(Comparator.comparing(Metric::getKey))
            .forEach(System.out::println);
        return null;
    }

}
