# maas-rest-client

[![Build Status](http://jenkins.activeeon.com/buildStatus/icon?job=maas-rest-client)](http://jenkins.activeeon.com/job/maas-rest-client)

A Java Rest client to consume MAAS (Metal As A Service) API.

The API documentation can be found [here](https://maas.ubuntu.com/docs/api.html).

We use the RestTemplate from Spring framework to consume the API.
Responses are received in JSON format.
To retrieve and transfert content from MAAS API, we mainly use POJOs (Plain Old Java Objects) which are automatically encoded/decoded by Spring.

Some features are missing (such as Nodes/Networks/Zones management for instance) due to lack of usage. Nevertheless, new methods can be easily implemented into `MaasClient.java` following numerous examples provided.

## Get API Key

The API key is represented as a single string composed by 3 different tokens (separated by ':'). The corresponding tokens are:

 - The consumer key
 - The access key
 - The access secret

API key = `<consumer_key>:<access_key>:<access_secret>`

Strangely, the consumer secret is not part of the API key and must be left as an empty string.
As of now (API v2.0), the authentication is performed through the old OAuth 1 mechanism.
Therefore, using the API key is the only way to consume the API for now (we are still waiting for a MAAS OAuth upgrade to allow standard credentials based login and better tokens management).

### Further informations

 * How to retrieve the key and use the CLI: https://maas.ubuntu.com/docs/maascli.html
 * Authentication details and API key composition: https://maas.ubuntu.com/docs/api_authentication.html

## Usage examples

### Get started

*Note:* To avoid internal blocking errors, we do not delegate HTTP error handling to Spring RestTemplate.
Errors are only shown through logging mechanism.
Therefore, returned objects should always be null-checked to see if everything went fine.


#### Connect to a local maas server providing self-signed certificate:

```java
MaasClient maasClient = new MaasClient(
        "https://192.168.X.Y/MAAS/api/2.0",
        "xxxxxxxxxxxxxxxxxx:yyyyyyyyyyyyyyyyyy:zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
        true
);
```

#### Retrieve and print the name of the MAAS server:

```java
String maasName = maasClient.getConfig("maas_name");
System.out.println(maasName);
```

#### Print the full MAAS version

```java
MaasVersion maasVersion = maasClient.getMaasVersion();
System.out.println(maasVersion.getVersion() + "/" + maasVersion.getSubversion());
```


#### Retrieve machines informations

*Note:* `Machine` parameters (as well as all POJOs) can be found in `data` folder.

```java
// Retrieve all machines
List<Machine> machines = maasClient.getMachines();

// Retrieve a specific machine with id "xyz"
Machine machine = maasClient.getMachineById("xyz");

// Retrieve a specific machine with hostname "nodeX"
Machine machine = maasClient.getMachineByName("nodeX");
```

### Manipulate machines

#### Power on/off machines

```java
// Try soft shutdown first on machine "xyz"
if (!maasClient.powerOffMachine("xyz", "soft")) {
    // If soft failed, do a hard shutdown
    if (!maasClient.powerOffMachine("xyz")) {
        // Unable to power off machine
    }
}

...

// Boot the machine with a specific comment
maasClient.powerOnMachine("xyz", "Automatically power on by Java Rest client");
```

#### Create a new machine

*Note:* Currently, the only supported power types are IPMI and Virsh.

```java
// Create a new machine using Virsh power type (testing purpose)
Machine machine = maasClient.createMachine(new Machine.Builder()
    .hostName("testVM")
    .macAddress("54:52:00:11:22:33")
    .architecture("amd64")
    .subArchitecture("generic")
    .powerType(VirshPowerType.builder()
            .powerAddress("qemu:///system")
            .powerId("testVM")
            .build()
    )
);
```

When the machine is created, MAAS automatically starts to commission it.
This operation can last a few minutes (6 minutes needed to commission a Virsh machine having 8GB disk).
The new machine must be then allocated (fast operation) and finally deployed (OS installation, 8 minutes needed to deploy an Ubuntu 16.04 LTS).
If deployment is busy waiting for VM to boot, then you need to power Off/On the machine before deploying the OS.

#### Commission, Allocate, Deploy, and Release a machine

```java
Machine machine;

// Commission
machine = maasClient.commissionMachine("xyz");

// Allocate
machine = maasClient.allocateMachineById("xyz");

// Deploy with a specific comment
machine = maasClient.deployMachine("xyz", null, null, "Deployment from MAAS Java REST client");

// Release
if (!maasClient.releaseMachineById("xyz")) {
    // Unable to release machine with id "xyz"
}
```

### Manage commissioning scripts

#### Upload a new commissioning script

```java
CommissioningScript script;

// Upload from inline text
StringBuilder scriptData = new StringBuilder();
scriptData.append("#!/bin/bash\n")
          .append("echo 'It works fine!'\n");
script = maasClient.postCommissioningScript(scriptData.getBytes()), "testScript.sh");

// Upload from existing file
script = maasClient.postCommissioningScript("/path/to/existing/script");
```

#### Get the list of existing scripts and retrieve them

```java
// Retrieve first the name of the scripts
List<String> scriptsName maasClient.getCommissioningScripts();

byte[] scriptContent;
if (scriptsName != null && !scriptsName.isEmpty()) {
    maasClient.getCommissioningScript()
    scriptContent = getCommissioningScriptByName(scriptsName.get(0)).getByteArray();
    ...
}
```
