# Classmethod Tupl Storage Backend for Titan

> Titan: Distributed Graph Database is a scalable graph database optimized for
> storing and querying graphs containing hundreds of billions of vertices and
> edges distributed across a multi-machine cluster. Titan is a transactional
> database that can support thousands of concurrent users executing complex
> graph traversals in real time. --
> [Titan Homepage](http://thinkaurelius.github.io/titan/)

> Tupl: The Unnamed Persistence Library is a high-performance, concurrent,
> transactional, scalable, low-level embedded database. Features include
> record-level locking, upgradable locks, deadlock detection, cursors, hot
> backups, striped files, encryption, pluggable replication, nested
> transaction scopes, and direct lock control. Although written in Java,
> Tupl doesn't suffer from garbage collection pauses when configured with
> a large cache.  -- [Tupl Homepage](https://github.com/cojen/Tupl)

Titan + Tupl = Embedded Graph Database - Storage Management

## Features
The following is a list of features of the Classmethod Tupl Storage Backend for Titan.
* AWS managed authentication and authorization.
* Configurable storage directory allow multiple graphs to be stored in a single
host in the same region.
* Integrated with Titan metrics.
* Titan 1.0.0 and Tinkerpop 3.0.1-incubating compatibility.
* All the store features titan-berkeleyje but with improved performance of Tupl
across the board.
* Permissive Apache 2.0 license both for this Storage Backend and Tupl.

## Getting Started
This example populates a Titan graph database backed by Tupl using the
[Marvel Universe Social Graph](https://aws.amazon.com/datasets/5621954952932508).
The graph has a vertex per comic book character with an edge to each of the
comic books in which they appeared.

### Load a subset of the Marvel Universe Social Graph
1. Clone the repository in GitHub.

    ```
    git clone https://github.com/classmethod/tupl-titan-storage-backend.git
    ```
2. Run the `install` target to copy some dependencies to the target folder.

    ```
    mvn install
    ```
3. Clean up old ElasticSearch indexes.

    ```
    rm -rf /tmp/searchindex
    ```
4. Install Titan Server with the Classmethod Tupl Storage Backend for Titan, which
includes Gremlin Server.

    ```
    src/test/resources/install-gremlin-server.sh
    ```
5. Change directories to the Gremlin Server home.

    ```
    cd server/tupl-titan100-storage-backend-1.0.0-hadoop1
    ```
6. Start Gremlin Server:

    ```
    bin/gremlin-server.sh ${PWD}/conf/gremlin-server/gremlin-server.yaml
    ```
7. Start a Gremlin shell with `bin/gremlin.sh` and connect to the Gremlin Server
endpoint.

    ```
    :remote connect tinkerpop.server conf/remote.yaml
    ```
8. Load the first 100 lines of the Marvel graph using the Gremlin shell.

    ```
    :> jp.classmethod.titan.example.MarvelGraphFactory.load(graph, 100, false)
    ```
9. Print the characters and the comic-books they appeared in where the
characters had a weapon that was a shield or claws.

    ```
    :> g.V().as('character').has('weapon', within('shield','claws')).out('appeared').as('comic-book').select('character','comic-book')
    ```
10. Print the characters and the comic-books they appeared in where the
characters had a weapon that was not a shield or claws.

    ```
    :> g.V().as('character').has('weapon', without('shield','claws')).out('appeared').as('comic-book').select('character','comic-book')
    ```
11. Print a sorted list of the characters that appear in comic-book AVF 4.

    ```
    :> g.V().has('comic-book', 'AVF 4').in('appeared').values('character').order()
    ```
12. Print a sorted list of the characters that appear in comic-book AVF 4 that
have a weapon that is not a shield or claws.

    ```
    :> g.V().has('comic-book', 'AVF 4').in('appeared').has('weapon', without('shield','claws')).values('character').order()
    ```

### Complete the TinkerPop tutorial
1. Repeat steps 1 through 5, and step 7 of the Marvel graph section.
2. Now you can follow along the [TinkerPop Tutorial](http://tinkerpop.apache.org/docs/3.1.1-incubating/tutorials/getting-started/). When the tutorial and documentation refer to the `TinkerFactory.createModern()`, you can create the graph with: `jp.classmethod.titan.example.TuplTinkerFactory.createModern()`. When the tutorial and documentation refer to the baby graph, you can create the graph with: `jp.classmethod.titan.example.TuplTinkerFactory.creatBaby()`. TuplTinkerFactory adds schema elements and makes adjustments to the original graph that are necessary for working through the tutorial with the Classmethod Tupl Storage Backend for Titan.

### Load the Graph of the Gods
1. Repeat steps 1 through 7 of the Marvel graph section.
2. Load the Graph of the Gods.

    ```
    :> com.thinkaurelius.titan.example.GraphOfTheGodsFactory.load(graph)
    ```
3. Now you can follow the rest of the
[Titan Getting Started](http://s3.thinkaurelius.com/docs/titan/1.0.0/getting-started.html#_global_graph_indices)
documentation, starting from the Global Graph Indeces section. You need to
prepend each command with `:>` for remotely executing the commands on the
Gremlin Server endpoint. Also whenever you remotely execute traversals that
include local variables in steps, those local variables need to be defined in
the same line before the traversal. For example, to run the traversal
`g.V(hercules).out('father', 'mother').values('name')` remotely, you would need
to prepend it with the definition for Hercules:

    ```
    :> hercules = g.V(saturn).repeat(__.in('father')).times(2).next(); g.V(hercules).out('father', 'mother').values('name')
    ```
Note that the definition of Hercules depends on the definition of Saturn, so you
would need to define Saturn first:

    ```
    :> saturn = g.V().has('name', 'saturn').next(); hercules = g.V(saturn).repeat(__.in('father')).times(2).next(); g.V(hercules).out('father', 'mother').values('name')
    ```
The reason these need to be prepended is that local variable state is not
carried over for each remote script execution, except for the variables defined
in the scripts that run when Gremlin server is turned on. See the
`scriptEngines/gremlin-groovy/scripts` list element in the Gremlin Server YAML
file for more information.
4. Alternatively, repeat steps 1 through 8 of the Marvel graph section and
follow the examples in the
[TinkerPop documentation](http://tinkerpop.incubator.apache.org/docs/3.0.1-incubating/#_mutating_the_graph),
prepending each command with `:>` for remote execution. Skip the
`TinkerGraph.open()` step as the remote execution environment already has a
`graph` variable set up.

### Run Gremlin on Gremlin Server in EC2 using a CloudFormation template
The Classmethod Tupl Storage Backend for Titan includes a CloudFormation template that
creates a VPC, an EC2 instance in the VPC, installs Gremlin Server with the
Classmethod Tupl Storage Backend for Titan installed, and starts the Gremlin Server
websockets endpoint. The Network ACL of the VPC includes just enough access to
allow:

 - you to connect to the instance using SSH and create tunnels (SSH inbound)
 - the EC2 instance to download yum updates from central repositories (HTTP
   outbound)
 - the EC2 instance to download your tupl.properties file and the Gremlin Server
   package from S3 (HTTPS outbound)
 - the ephemeral ports required to support the data flow above, in each
   direction

Requirements for running this CloudFormation template include two items.

 - You require an SSH key for EC2 instances must exist in the region you plan to
   create the Gremlin Server stack.
 - You need to have created an IAM role in the region that has S3 Read access,
   the very minimum policies required to run this CloudFormation stack.
   S3 read access is required to provide the tupl.properties
   file to the stack in cloud-init.

Note, this cloud formation template downloads repackaged versions of the Titan zip
files available on the
[Titan downloads page](https://github.com/thinkaurelius/titan/wiki/Downloads).
We repackaged these zip files in order to include the Classmethod Tupl Storage Backend
for Titan and its dependencies. The
[repackaged version of the Titan zip](https://s3-us-west-2.amazonaws.com/titan-tupl-us-west-2/tupl-titan100-storage-backend-1.0.1-hadoop1.zip)'s
SHA-256 hash is `27915a82bac4d9bd1793efc4c7c98be68280be420a30f27994b9aea480c69e3b`.

1. Click [<img src="http://docs.aws.amazon.com/ja_jp/amazondynamodb/latest/developerguide/images/cloudformation-launch-stack-button.png">](https://console.aws.amazon.com/cloudformation/home?region=us-west-2#cstack=sn~ClassmethodTuplTitanGremlinServer|turl~https://s3-us-west-2.amazonaws.com/titan-tupl-us-west-2/tupl-titan100-storage-backend-cfn.json) to launch the stack in Oregon (the usual Amazon EC2 and EBS charges will apply). The SHA-256 hash of the
CloudFormation script is `27915a82bac4d9bd1793efc4c7c98be68280be420a30f27994b9aea480c69e3b`.
2. On the Specify Parameters page, you need to specify the following:
  * The size of the EBS volume used to store the graph
  * EC2 Instance Type
  * The network whitelist pattern for Gremlin Server Websockets port
  * The Gremlin Server port, default 8182.
  * The S3 URL to your tupl.properties configuration file
  * The name of your pre-existing EC2 SSH key
  * The network whitelist for the SSH protocol. You will need to allow incoming
  connections via SSH to enable the SSH tunnels that will secure Websockets connections
  to Gremlin Server.
  * The path to an IAM role that has the minimum amount of privileges to run this
  CloudFormation script and run Gremlin Server with the Classmethod Tupl Storage Backend for
  Titan. This role will require S3 read access to get the tupl.properties file.
3. On the Options page, click Next.
4. On the Review page, select "I acknowledge that this template might cause AWS
CloudFormation to create IAM resources." Then, click Create.
5. Create an SSH tunnel from your localhost port 8182 to the Gremlin Server port (8182)
on the EC2 host after the stack deployment is complete. The SSH tunnel command
is one of the outputs of the CloudFormation script so you can just copy-paste it.
6. Repeat steps 4, 5, and 7 of the Marvel graph section above.

## Tupl Specific Configuration
Each configuration option has a certain mutability level that governs whether
and how it can be modified after the database is opened for the first time. The
following listing describes the mutability levels.

1. **FIXED** - Once the database has been opened, these configuration options
cannot be changed for the entire life of the database
2. **GLOBAL_OFFLINE** - These options can only be changed for the entire
database cluster at once when all instances are shut down
3. **GLOBAL** - These options can only be changed globally across the entire
database cluster
4. **MASKABLE** - These options are global but can be overwritten by a local
configuration file
5. **LOCAL** - These options can only be provided through a local configuration
file

Leading namespace names are shortened and sometimes spaces were inserted in long
strings to make sure the tables below are formatted correctly.

### General Tupl Configuration Parameters
All of the following parameters are in the `storage` (`s`) namespace, and most
are in the `storage.tupl` (`s.t`) namespace subset.

| Name            | Description | Datatype | Default Value | Mutability |
|-----------------|-------------|----------|---------------|------------|
| `s.backend` | The primary persistence provider used by Titan. To use Tupl you must set this to `jp.classmethod.titan.diskstorage. tupl.TuplStoreManager` | String |  | MASKABLE |
| `s.directory` | The storage directory for tupl. If not set, Tupl will run as a volatile, in-memory store that is not persisted to disk. | String |  | LOCAL |
| `s.t.prefix` | A file prefix to include in the database files Tupl creates for this graph. | String | tupldb | FIXED |
| `s.t.map-data-files` | Enable memory mapping of the data files. Entire graph needs to fit in memory. | Boolean | false | MASKABLE |
| `s.t.min-cache-size` | The tupl minimum cache size (bytes). Must be at least 5 pages long. | Long | 100000000 | MASKABLE |
| `s.t.secondary-cache-size` | The tupl secondary cache size (bytes). Off by default. | Long | 0 | MASKABLE |
| `s.t.durability-mode` | Default transaction durability mode. | String | SYNC | MASKABLE |
| `s.t.sync-writes` | Set true to ensure all writes to the main database file are immediately durable, although not checkpointed. This option typically reduces overall performance, but checkpoints complete more quickly. As a result, the main database file requires less pre-allocated pages and is smaller. | Boolean | false | MASKABLE |
| `s.t.page-size` | The page size in bytes. | Integer | 4096 | MASKABLE |
| `s.t.direct-page-access` | Set true to allocate all pages off the Java heap, offering increased performance and reduced garbage collection activity. | Boolean | true | MASKABLE |

### Tupl Locking Configuration Parameters
All of the following parameters are in the `storage.tupl.lock` (`s.t.l`) namespace.

| Name            | Description | Datatype | Default Value | Mutability |
|-----------------|-------------|----------|---------------|------------|
| `s.t.l.mode` | Default lock mode. READ_UNCOMMITTED is needed to pass most Titan KV and KCV tests. | String | READ_UNCOMMITTED | MASKABLE |
| `s.t.l.upgrade-rule` | Default lock upgrade rule. | String | STRICT | MASKABLE |
| `s.t.l.timeout` | The lock timeout (milliseconds). | Long | 60000 | MASKABLE |

### Tupl Checkpointing Configuration Parameters
All of the following parameters are in the `storage.tupl.checkpoint` (`s.t.c`) namespace.

| Name            | Description | Datatype | Default Value | Mutability |
|-----------------|-------------|----------|---------------|------------|
| `s.t.c.rate` | The checkpoint rate in milliseconds. Set to a negative number to disable automatic checkpoints. | String | 1000 | MASKABLE |
| `s.t.c.delay-threshold` | The checkpoint delay threshold in milliseconds (infinite if negative). This delay takes precedence over the size threshold. Set to zero for non-transactional operations. | String | 60000 | MASKABLE |
| `s.t.c.size-threshold` | The checkpoint size threshold in bytes. Set to zero for non-transactional operations. | Long | 1073741824 | MASKABLE |

## Run all tests against Tupl on an EC2 Amazon Linux AMI
1. Install dependencies. For Amazon Linux:

    ```
    sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
    sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
    sudo yum update -y && sudo yum upgrade -y
    sudo yum install -y apache-maven git java-1.8.0-openjdk-devel
    sudo alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java
    sudo alternatives --set javac /usr/lib/jvm/java-1.8.0-openjdk.x86_64/bin/javac
    git clone https://github.com/classmethod/tupl-titan-storage-backend.git
    sudo mkdir -p /usr/local/packages/tupl-titan100-storage-backend-1.0.0-hadoop1/data
    sudo chown -r ec2-user:ec2-user /usr/local/packages/tupl-titan100-storage-backend-1.0.0-hadoop1/data
    ```
2. Open a screen so that you can log out of the EC2 instance while running tests
with `screen`.
3. Run the tests.

    ```
    mvn test -Pintegration-tests -Dproperties-file=src/test/resources/tupl.properties > o 2>&1
    ```
4. Exit the screen with `CTRL-A D` and logout of the EC2 instance.
5. Monitor the CPU usage of your EC2 instance in the EC2 console.
When CPU usage goes to zero, that means the tests are done.
6. Log back into the EC2 instance and resume the screen with `screen -r` to
review the test results.

    ```
    cd target/surefire-reports && grep testcase *.xml | grep -v "\/"
    ```
7. Terminate the instance when done.
