#include <iostream>
#include <string>
#include <time.h>
#include "harpc/ThriftClientPool.h"
#include "thrift/EchoService.h"

using namespace std;
using namespace bfd::harpc;

int main(int argc, char** argv) {
    if(argc < 2) {
        std::cout << "Usage: test-num" << std::endl;
        exit(1);
    }

    char * times_ptr = argv[1];
    int times = atoi(times_ptr);

    cout << "will test " << times << " times..." << endl;

    std::string confPath = "conf.xml";
    ThriftClientPool <bfd::harpc::demo::EchoServiceClient> *ms_client_pool =
            new ThriftClientPool<bfd::harpc::demo::EchoServiceClient>(confPath);

    cout << "Begin invoke." << endl;

    time_t start, end;
    start = time(NULL);

    for (int i = 0; i < times; ++i) {
        string result;

        // 匿名的 functor，接受参数 clientPtr，返回的是 { body }
        // 对应这里是 (t, ...) { t->echo(result, "Hello, service.") }
        // 另外请参阅 c++11 lambda
        ms_client_pool->invoke([&](bfd::harpc::demo::EchoServiceClient *clintPtr, int param1, int param2) {
            clintPtr->echo(result, "Hello, service.");
            // cout << "param1: " << param1 << "\tparam2: " << param2 << endl;
        }, 111, 222);

        // cout << "End get Data.result:" << result << endl;
    }

    end = time(NULL);

    cout << "cost time: " << (end - start) << " seconds" << endl;

    return 0;
}