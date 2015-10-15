#include <atomic>
#include <sstream>

#include "thrift/EchoService.h"

#ifndef DEMO_SERVICE_DEMOSERVICE_H
#define DEMO_SERVICE_DEMOSERVICE_H

using namespace std;

class DemoService : virtual public bfd::harpc::demo::EchoServiceIf {
    std::atomic_long count_;

public:
    DemoService()
            : count_(0) {
    }

public:
    void echo(std::string &_return, const std::string &msg) {
        stringstream ss;
        ss << count_++;
        string str = ss.str();

        _return = "Hello, you are client" + ss.str();
    }
};


#endif //DEMO_SERVICE_DEMOSERVICE_H
