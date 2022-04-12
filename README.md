Digital Twin Modeling Framework
======
[![License Badge](https://img.shields.io/badge/license-EPL%202.0-brightgreen.svg)](https://opensource.org/licenses/EPL-2.0)

## About

A Digital Twin framework that allows connecting UML models to physical devices to perform analysis and validation tasks.

## Overview

The structure of this repository is the following:

### [carController](https://github.com/atenearesearchgroup/digitalTwinModelingFramework/tree/main/carConnector)

This module is in charge of the car's movement. It retrieves the sensors' data and sends it via Bluetooth to the computer. Once it is received, it is forwarded to the carConnector using a socket.

The car used in our development is a Lego Mindstorms NXT, and we used the firmware [LeJOS](https://web.archive.org/web/20210507030446/http://lejos.org/) to define its behavior.

### [physicalTwinConnector](https://github.com/atenearesearchgroup/digitalTwinModelingFramework/tree/main/carConnector)

This module takes the raw data from the carController and stores it in the data lake.

### [digitalTwinConnector](https://github.com/atenearesearchgroup/digitalTwinModelingFramework/tree/main/useConnector)

This module is a USE plugin in charge of the bidirectional connections to the data lake. This module takes the necessary information from the models in USE and stores it in the data lake and vice versa.

### [tracesMonitoring](https://github.com/atenearesearchgroup/digitalTwinModelingFramework/tree/main/tracesMonitoring)

This module is another USE plugin in charge of bidirectional connections to the data lake. This plugin retrieves traces from the Physical and Digital Twins and compares them to find any mismatches to validate the system's correct behavior.

### [useModels](https://github.com/atenearesearchgroup/digitalTwinModelingFramework/tree/main/useModels)

These are the use models used in our project to test and validate the previous plugins.
