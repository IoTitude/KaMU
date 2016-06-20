# KaMU

KaMU is a an application used by a embedded measuring unit solution to perform variety of measurements related to sewer managemenet.

## Tools

Kaa 0.9.0

Oracle JDK 8

MariaDB 5.5

Zookeeper 3.4.5

Cassandra 3.5

Baasbox 0.9.5

OrientDB 1.7.10

## Versioning

KaMU software will use customised version numbering explained here.

major.minor.status.week

0.1.0.24

status:
0 alpha  
1 beta  
2 release candidate  
3 release  

## Release Plan

### Release Plan for 0.1

| Feature | Status |
|:----|:----|
| Send random log data to Cassandra | Done |

### Release Plan for 0.2

| Feature | Status |
|:----|:----|
| Profiling logic | Done |
| Add MAC address to log data | Done |
| Read log data from SimDataGen | Done |

### Release Plan for 0.3

| Feature | Status |
|:----|:----|
| Threading | Done |
| Communicating with BaasBox | Done |

### Release Plan for 0.4

| Feature | Status |
|:----|:----|
| - | - |

### Other features, tbd

* Ability to measure using different sensors
* Updating on the fly
* Ability to restart KaMU at any point
* Tracking KaMU condition
 * Eg. battery level
