#include <iostream>
#include <stdlib.h>
#include <thrift/concurrency/ThreadManager.h>//link:thrift
#include <thrift/concurrency/PosixThreadFactory.h>
#include <thrift/server/TThreadedServer.h>
#include <thrift/server/TThreadPoolServer.h>
#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSocket.h>
#include <thrift/transport/TBufferTransports.h>
#include <boost/shared_ptr.hpp>

#include "thrift/EchoService.h"
#include "DemoService.h"

using namespace std;

using namespace ::apache::thrift::concurrency;
using namespace ::apache::thrift::server;
using namespace ::apache::thrift::transport;
using namespace ::apache::thrift::protocol;
using  namespace bfd::harpc::demo;

boost::shared_ptr<TServerTransport> serverTransport;
boost::shared_ptr<TThreadedServer> serverPtr;

void run(int port) {
    int thread_num = 10;

    boost::shared_ptr<DemoService> handler(
            new DemoService());
    boost::shared_ptr < TProcessor> processor(new EchoServiceProcessor(handler));

    serverTransport.reset(new TServerSocket(port));

    boost::shared_ptr < TTransportFactory> transportFactory(new TBufferedTransportFactory());
    boost::shared_ptr < TProtocolFactory> protocolFactory(new TBinaryProtocolFactory());
    boost::shared_ptr < ThreadManager > threadManager = ThreadManager::newSimpleThreadManager(thread_num);
    boost::shared_ptr < PosixThreadFactory > threadFactory = boost::shared_ptr< PosixThreadFactory > (new PosixThreadFactory());

    threadManager->threadFactory(threadFactory);
    threadManager->start();

    serverPtr.reset(
            new TThreadedServer(processor, serverTransport, transportFactory, protocolFactory));

    serverPtr->serve();
}

int main(int argc, char ** argv) {
    if(argc < 2) {
        std::cout << "Usage: port" << std::endl;
        exit(1);
    }

    char * port_ptr = argv[1];
    int port = atoi(port_ptr);

    cout << "Begin run, port is: " << port << endl;

    run(port);

    return 0;
}