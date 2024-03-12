# Distributed Snapshot Algorithm

Implementation of the Chandy-Lamport distributed snapshot algorithm [[1]](https://dl.acm.org/doi/abs/10.1145/214451.214456), with the management of concurrent initiators through the intersection approach by the Spezialetti and Kearns' algorithm [[2]](https://ieeexplore.ieee.org/abstract/document/346149?casa_token=_YUh8tiU2I4AAAAA:x3IKI6uxZbQDqeV1IeoIXXGAGQ3mYy-eCpO8rlmvfDv8r_ajgTcqi7PbjZWeqV5MRu41vXE).

# Code Description
The main files that build up the project are:
- [**_Neighbor.java_**](https://github.com/LeoGori/distributed-snapshot/blob/master/src/Neighbor.java): class that represent the neighbor of the node, containing the information about its IP address and port it is listening to.
- [**_Node.java_**](https://github.com/LeoGori/distributed-snapshot/blob/master/src/Node.java): class that inherits from **Neighbor**, that represents the current machine the algorithm is running on. This class contains instances of **Sender**, **ReceiverThread** and **MultiCastReceiver** classes, that respectively manage the delivery of messages to the neighbors, the reception of messages from the neighbors and the reception of discovery messages for network initialization.
- [**_Snapshot.java_**](https://github.com/LeoGori/distributed-snapshot/blob/master/src/Snapshot.java): class that represents the relevant information contained in a local snapshot.
- [**_SnapshotNode.java_**](https://github.com/LeoGori/distributed-snapshot/blob/master/src/SnapshotNode.java): class inheriting from Node, it contains an instance of the class Snapshot, that records its partial view of the global state.
- [**_Tester.java_**](https://github.com/LeoGori/distributed-snapshot/blob/master/src/Tester.java): class that gathers the partial view of the SnapshotNodes for a given session, and tests the integrity of the global snapshot.
- [**_main.cpp_**](https://github.com/LeoGori/distributed-snapshot/blob/master/src/Main.java): the entry point of the program, contains the code to initialize the network, testing that messages are delivered correctly, and to initiate the snapshot in manual and automatic modes.

# Language and APIs
The code is entirely written in Java programming language, with the use of the DatagramPacket, InetAddress and Sockets packages for communication. The Observer design pattern, employing a pull mechanism, is implemented to manage the reception of messages by the subject (receiver) and handle them within Node instances (observers).
