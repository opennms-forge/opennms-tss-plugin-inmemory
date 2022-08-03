# Time Series Storage - InMemory [![CircleCI](https://circleci.com/gh/opennms-forge/opennms-tss-plugin-inmemory.svg?style=svg)](https://circleci.com/gh/opennms-forge/opennms-tss-plugin-inmemory)

This plugin exposes a simple implementation of the TimeSeriesStorage interface.
It holds the data in a Guava cache.
It can be used in OpenNMS to store and retrieve timeseries data.

This implementation is meant to be a demonstration of how to implement the interface and for testing purposes.
It is not meant for production use.

### Usage
* compile: ``mvn install``
* activation: Enable the timeseries integration layer: see [documentation](https://docs.opennms.com/horizon/latest/deployment/time-series-storage/timeseries/configuration.html)
* activate in Karaf shell: ``bundle:install -s mvn:org.opennms.plugins.tss/inmemory/2.0.0-SNAPSHOT``
* 
#### Karaf commands
* show statistics: ``opennms-tss-inmemory:stats``
* show all metrics: ``opennms-tss-inmemory:metrics``

  
 



