# Lightstreamer - Round-Trip Demo - Java Adapter

<!-- START DESCRIPTION lightstreamer-example-roundtrip-adapter-java -->

This project shows the Round-Trip Demo Data and Metadata Adapters and how they can be plugged into Lightstreamer Server and used to feed the [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-RoundTrip-client-javascript) front-end.
The *Round-Trip Demo* is a simple broadcast messages application based on Lightstreamer.

## Details

The project is comprised of source code and a deployment example. 

### Dig the Code

The source code is divided into two folders.

#### Round-Trip Data Adapter
Contains the source code for the Round-Trip Data Adapter. This Adapter broadcasts to all connected users the messages received from the Metadata Adapter for the five defined items in real-time.<br>

#### Metadata Adapter
Contains the source code for a Metadata Adapter to be associated with the Round-Trip Demo Data Adapter. This Metadata Adapter inherits from the reusable `LiteralBasedProvider` in [Lightstreamer Java In-Process Adapter SDK](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#literalbasedprovider-metadata-adapter) and it plays the extra-role of receiving messages from the clients and forwarding them to the Data Adapter.<br>
It should not be used as a reference for a real case of client-originated message handling, as no guaranteed delivery and no clustering support is shown.
<br>

See the source code comments for further details.

<!-- END DESCRIPTION lightstreamer-example-roundtrip-adapter-java -->

### The Adapter Set Configuration

This Adapter Set is configured and will be referenced by the clients as `ROUNDTRIPDEMO`. 

The `adapters.xml` file for the *Round-Trip Demo*, should look like:
```xml      
<?xml version="1.0"?>
<adapters_conf id="ROUNDTRIPDEMO">

<metadata_adapter_initialised_first>Y</metadata_adapter_initialised_first>

  <metadata_provider>

	  <adapter_class>com.lightstreamer.examples.roundtrip_demo.adapters.RoundTripMetadataAdapter</adapter_class>

	  <!-- Optional, managed by the inherited LiteralBasedProvider.
		   See LiteralBasedProvider javadoc. -->
	  <!--
	  <param name="max_bandwidth">40</param>
	  <param name="max_frequency">3</param>
	  <param name="buffer_size">30</param>
	  <param name="prefilter_frequency">5</param>
	  <param name="allowed_users">user123,user456</param>
	  <param name="distinct_snapshot_length">30</param>
	  -->

	  <!-- Optional, managed by the inherited LiteralBasedProvider.
		   See LiteralBasedProvider javadoc. -->
	  <param name="item_family_1">roundtrip\d{1,2}</param>
	  <param name="modes_for_item_family_1">MERGE</param>

  </metadata_provider>


  <data_provider name="ROUNDTRIP_ADAPTER">

	  <adapter_class>com.lightstreamer.examples.roundtrip_demo.adapters.RoundTripDataAdapter</adapter_class>

  </data_provider>


</adapters_conf>
```

<i>NOTE: not all configuration options of an Adapter Set are exposed by the file suggested above. 
You can easily expand your configurations using the generic template, see the [Java In-Process Adapter Interface Project](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#configuration) for details.</i><br>
<br>
Please refer [here](https://lightstreamer.com/docs/ls-server/latest/General%20Concepts.pdf) for more details about Lightstreamer Adapters.<br>

## Install

If you want to install a version of the *Round-Trip Demo* in your local Lightstreamer Server, follow these steps:

* Download *Lightstreamer Server* (Lightstreamer Server comes with a free non-expiring demo license for 20 connected users) from [Lightstreamer Download page](http://www.lightstreamer.com/download.htm), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
* Make sure that Lightstreamer Server is not running.
* Get the `deploy.zip` file of the [latest release](https://github.com/Lightstreamer/Lightstreamer-example-RoundTrip-adapter-java/releases), unzip it, and copy the `RoundTripDemo` folder into the `adapters` folder of your Lightstreamer Server installation.
* [Optional] Customize the logging settings in log4j configuration file: `RoundTripDemo/classes/log4j2.xml`.
* Launch Lightstreamer Server.
* Test the Adapter, launching the [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-RoundTrip-client-javascript) listed in [Clients Using This Adapter](#clients-using-this-adapter).

## Build

To build your own version of `roundtrip-adapter-java-x.y.z.jar` instead of using the one provided in the `deploy.zip` file from the [Install](#install) section above, you have two options:
either use [Maven](https://maven.apache.org/) (or other build tools) to take care of dependencies and building (recommended) or gather the necessary jars yourself and build it manually.
For the sake of simplicity only the Maven case is detailed here.

### Maven

You can easily build and run this application using Maven through the pom.xml file located in the root folder of this project. As an alternative, you can use an alternative build tool (e.g. Gradle, Ivy, etc.) by converting the provided pom.xml file.

Assuming Maven is installed and available in your path you can build the demo by running
```sh 
 mvn install dependency:copy-dependencies 
```

## See Also

### Clients Using this Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-RoundTrip-client-javascript)
* [Lightstreamer - Basic Stock-List and Round-trip Demo - Java ME Client](https://github.com/Lightstreamer/Lightstreamer-example-StockList-client-midlet)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [LiteralBasedProvider Metadata Adapter](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#literalbasedprovider-metadata-adapter)
* [Lightstreamer - Basic Chat Demo - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-Chat-adapter-java)
* [Lightstreamer - Basic Chat Demo - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-Chat-client-javascript)

## Lightstreamer Compatibility Notes


- Compatible with Lightstreamer SDK for Java In-Process Adapters since 7.3.
- For a version of this example compatible with Lightstreamer SDK for Java Adapters version 6.0, please refer to [this tag](https://github.com/Lightstreamer/Lightstreamer-example-ChatTile-adapter-java/tree/pre_mvn).
- For a version of this example compatible with Lightstreamer SDK for Java Adapters version 5.1, please refer to [this tag](https://github.com/Lightstreamer/Lightstreamer-example-RoundTrip-adapter-java/tree/for_Lightstreamer_5.1).
