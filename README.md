# Lightstreamer - Round-Trip Demo - Java Adapter

<!-- START DESCRIPTION lightstreamer-example-roundtrip-adapter-java -->

This project shows the Round-Trip Demo Data and Metadata Adapters and how they can be plugged into Lightstreamer Server and used to feed the [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-RoundTrip-client-javascript) front-end.
The *Round-Trip Demo* is a simple broadcast messages application based on Lightstreamer.

## Details

The project is comprised of source code and a deployment example. 

### Dig the Code

The source code is divided into two folders.

#### Round-Trip Data Adapter
Contains the source code for the Round-Trip Data Adapter. This Adapter broadcasts to all connected users the messages received from the Metadata Adapter for the five defined items in real-time.<br>

#### Metadata Adapter
Contains the source code for a Metadata Adapter to be associated with the Round-Trip Demo Data Adapter. This Metadata Adapter inherits from the reusable `LiteralBasedProvider` in [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java) and it plays the extra-role of receiving messages from the clients and forwarding them to the Data Adapter.<br>
It should not be used as a reference for a real case of client-originated message handling, as no guaranteed delivery and no clustering support is shown.
<br>

See the source code comments for further details.

<!-- END DESCRIPTION lightstreamer-example-roundtrip-adapter-java -->

### The Adapter Set Configuration

This Adapter Set is configured and will be referenced by the clients as `ROUNDTRIPDEMO`. 

The `adapters.xml` file for the *Round-Trip Demo*, should look like:
```xml      
  <?xml version="1.0"?>

  <!-- Mandatory. Define an Adapter Set and sets its unique ID. -->
  <adapters_conf id="ROUNDTRIPDEMO">

    <!-- Mandatory. Define the Metadata Adapter. -->
    <metadata_provider>

        <!-- Mandatory. Java class name of the adapter. -->
        <adapter_class>roundtrip_demo.adapters.RoundTripMetadataAdapter</adapter_class>

        <!-- Optional for RoundTripMetadataAdapter.
             Configuration file for the Adapter's own logging.
             Logging is managed through log4j. -->
        <param name="log_config">adapters_log_conf.xml</param>
        <param name="log_config_refresh_seconds">10</param>

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


    <data_provider name="ROUNDTRIP">

        <!-- Mandatory. Java class name of the adapter. -->
        <adapter_class>roundtrip_demo.adapters.RoundTripDataAdapter</adapter_class>

        <!-- Optional for RoundTripDataAdapter.
             Configuration file for the Adapter's own logging.
             Leans on the Metadata Adapter for the configuration refresh.
             Logging is managed through log4j. -->
        <param name="log_config">adapters_log_conf.xml</param>

    </data_provider>

</adapters_conf>
```

Please refer [here](http://www.lightstreamer.com/latest/Lightstreamer_Allegro-Presto-Vivace_5_1_Colosseo/Lightstreamer/DOCS-SDKs/General%20Concepts.pdf) for more details about Lightstreamer Adapters.<br>

## Install

If you want to install a version of the *Round-Trip Demo* in your local Lightstreamer Server, follow these steps:

* Download *Lightstreamer Server* (Lightstreamer Server comes with a free non-expiring demo license for 20 connected users) from [Lightstreamer Download page](http://www.lightstreamer.com/download.htm), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
* Make sure that Lightstreamer Server is not running.
* Get the `deploy.zip` file of the [latest release](https://github.com/Weswit/Lightstreamer-example-RoundTrip-adapter-java/releases), unzip it, and copy the `roundtrip` folder into the `adapters` folder of your Lightstreamer Server installation.
* Copy the `ls-generic-adapters.jar` file from the `lib` directory of the sibling "Reusable_MetadataAdapters" SDK example to the `shared/lib` subdirectory in your Lightstreamer Server installation home directory.
* Launch Lightstreamer Server.
* Test the Adapter, launching the [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-RoundTrip-client-javascript) listed in [Clients Using This Adapter](https://github.com/Weswit/Lightstreamer-example-RoundTrip-adapter-java#clients-using-this-adapter).

## Build

To build your own version of `LS_roundtrip_data_adapter.jar` and ` LS_roundtrip_metadata_adapter.jar`, instead of using the one provided in the `deploy.zip` file from the [Install](https://github.com/Weswit/Lightstreamer-example-RoundTrip-adapter-java#install) section above, follow these steps:

* Download this project.
* Get the `ls-adapter-interface.jar`,`ls-generic-adapters.jar`, and `log4j-1.2.15.jar` files from the [latest Lightstreamer distribution](http://www.lightstreamer.com/download), and copy them into the `lib` directory.
* Create the jars `LS_roundtrip_metadata_adapter.jar` and `LS_roundtrip_data_adapter.jar` with commands like these:
```sh
 >javac -source 1.7 -target 1.7 -nowarn -g -classpath lib/log4j-1.2.15.jar;lib/ls-adapter-interface/ls-adapter-interface.jar;lib/ls-generic-adapters/ls-generic-adapters.jar -sourcepath src/src_data -d tmp_classes src/src_roundtrip/roundtrip_demo/adapters/RoundTripDataAdapter.java
 
 >jar cvf LS_roundtrip_data_adapter.jar -C tmp_classes src_data
 
 >javac -source 1.7 -target 1.7 -nowarn -g -classpath lib/log4j-1.2.15.jar;lib/ls-adapter-interface/ls-adapter-interface.jar;lib/ls-generic-adapters/ls-generic-adapters.jar;LS_messenger_data_adapter.jar -sourcepath src/src_metadata -d tmp_classes src/src_metadata/roundtrip_demo/adapters/RoundTripMetadataAdapter.java
 
 >jar cvf LS_roundtrip_metadata_adapter.jar -C tmp_classes src_metadata
```

## See Also

### Clients Using this Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-RoundTrip-client-javascript)
* [Lightstreamer - Basic Stock-List and Round-trip Demo - Java ME Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-midlet)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java)
* [Lightstreamer - Basic Chat Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Chat-adapter-java)
* [Lightstreamer - Basic Chat Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-Chat-client-javascript)

## Lightstreamer Compatibility Notes

* Compatible with Lightstreamer SDK for Java Adapters version 5.1.x
