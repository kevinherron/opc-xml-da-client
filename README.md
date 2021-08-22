OPC XML-DA Java Client
===================

OPC XML-DA Java Client, forked from [frederikhegger/opc-xmlda-sdk](https://github.com/frederikhegger/opc-xmlda-sdk) and updated for Java 11.

## Prerequisites

JDK 11, Maven 3.1+

## Generate SOAP models and build project

From the root project directory run: `mvn clean package`

## Usage examples

These examples run against the live demo server generously hosted by Advosol so please be kind and keep that in mind when testing.

### GetStatus
```java
class StatusExample {

    public static void main(String[] args) {
        OpcXmlDaClient client = OpcXmlDaClient.newBuilder()
            .setServerUrl("http://info.advosol.com/XMLDADemo/XML_Sim/opcxmldaserver.asmx")
            .build();

        GetStatusResponse response = client.getStatus();
        ServerStatus serverStatus = response.getStatus();
        System.out.printf("StatusInfo: %s%n", serverStatus.getStatusInfo());
        System.out.printf("VendorInfo: %s%n", serverStatus.getVendorInfo());
        System.out.printf("ProductVersion: %s%n", serverStatus.getProductVersion());
    }

}
```

Output:
```
StatusInfo: Started
VendorInfo: Advosol Inc., Advosol Inc.
ProductVersion: OPC XML-DA V1.0, XML DA Simulation Server
```

### Browse
```java
class BrowseExample {

    public static void main(String[] args) {
        OpcXmlDaClient client = OpcXmlDaClient.newBuilder()
            .setServerUrl("http://info.advosol.com/XMLDADemo/XML_Sim/opcxmldaserver.asmx")
            .build();

        browse(client, null, null, 0);
    }

    private static void browse(OpcXmlDaClient client, String parentItemPath, String parentItemName, int level) {
        BrowseResponse response = client.browse(request -> {
            request.setItemPath(parentItemPath);
            request.setItemName(parentItemName);
        });

        response.getElements().forEach(browseElement -> {
            String itemPath = browseElement.getItemPath();
            String itemName = browseElement.getItemName();

            for (int i = 0; i < level; i++) {
                System.out.print("  ");
            }
            if (browseElement.isIsItem()) {
                System.out.print("[*] ");
            }
            System.out.printf("%s%n", itemName);

            if (browseElement.isHasChildren()) {
                browse(client, itemPath, itemName, level + 1);
            }
        });
    }

}
```

Output:
```
SimulatedData
  [*] SimulatedData.Ramp
  [*] SimulatedData.Step
  [*] SimulatedData.Sine
  [*] SimulatedData.Random
  [*] SimulatedData.Signal
Static
  Static.Analog Types
    [*] Static.Analog Types.Int
    [*] Static.Analog Types.Double
    [*] Static.Analog Types.Int[]
    [*] Static.Analog Types.Double[]
  Static.Limited Access
    [*] Static.Limited Access.Read Only 1
    [*] Static.Limited Access.Read Only 2
    [*] Static.Limited Access.Write Only 1
    [*] Static.Limited Access.Write Only 2
  Static.Simple Types
    [*] Static.Simple Types.SByte
    [*] Static.Simple Types.Byte
    [*] Static.Simple Types.Short
    [*] Static.Simple Types.UShort
    [*] Static.Simple Types.Int
    [*] Static.Simple Types.UInt
    [*] Static.Simple Types.Long
    [*] Static.Simple Types.ULong
    [*] Static.Simple Types.Float
    [*] Static.Simple Types.Double
    [*] Static.Simple Types.DateTime
    [*] Static.Simple Types.Boolean
    [*] Static.Simple Types.String
  Static.Array Types
    [*] Static.Array Types.SByte[]
    [*] Static.Array Types.Byte[]
    [*] Static.Array Types.Short[]
    [*] Static.Array Types.UShort[]
    [*] Static.Array Types.Int[]
    [*] Static.Array Types.UInt[]
    [*] Static.Array Types.Long[]
    [*] Static.Array Types.ULong[]
    [*] Static.Array Types.Float[]
    [*] Static.Array Types.Double[]
    [*] Static.Array Types.DateTime[]
    [*] Static.Array Types.Boolean[]
    [*] Static.Array Types.String[]
  Static.Enumerated Types
    [*] Static.Enumerated Types.Gems
    [*] Static.Enumerated Types.Fellowship
Dynamic
  Dynamic.Analog Types
    Dynamic.Analog Types.Fools
      [*] Dynamic.Analog Types.Fools.Rosencrantz
      [*] Dynamic.Analog Types.Fools.Guildenstern
    [*] Dynamic.Analog Types.Int
    [*] Dynamic.Analog Types.Double
    [*] Dynamic.Analog Types.Int[]
    [*] Dynamic.Analog Types.Double[]
  Dynamic.Enumerated Types
    [*] Dynamic.Enumerated Types.Gems
    [*] Dynamic.Enumerated Types.Fellowship
ServerInfo
  [*] ServerInfo.ConnectedClients
  [*] ServerInfo.TotalGroups
  [*] ServerInfo.Shutdown Request
EventSources
  EventSources.Area1
    [*] EventSources.Area1.Simple
    [*] EventSources.Area1.Simple2
    [*] EventSources.Area1.Tracking
  EventSources.Area2
    [*] EventSources.Area2.Condition
[*] DW_INOUT1
```