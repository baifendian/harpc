/**
* Copyright (C) 2015 Baifendian Corporation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
#ifndef MYCONNECT_H_
#define MYCONNECT_H_

#include <boost/shared_ptr.hpp>
#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSocket.h>
#include <thrift/transport/TBufferTransports.h>
#include <string>
#include <sstream>

using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using ::apache::thrift::transport::TSocket;
using ::apache::thrift::transport::TBufferedTransport;

#define LOG2(a, b, c) ;


namespace bfd {
    namespace harpc {

        // 表示的是一个具体的连接
        template<typename T>
        class MyConnection {
            //;
        private:
            boost::shared_ptr<TTransport> transport_;
            boost::shared_ptr<T> client_;
            std::string ip_; // 本连接的 IP 地址
            int port_; // 本连接的 Port
            int conn_timeout_; // 连接超时时间
            int send_timeout_; // 发送超时时间
            int recv_timeout_; // 接受超时时间

            std::string conn_name_; // 表示 ip:port 字符串，实际意义不大

            bool status_; // 连接当前的状态，true 表示连接正常，false 表示连接异常

        public:
            MyConnection(const std::string &ip, int port, int timeout,
                         int send_timeout, int recv_timeout) :
                    ip_(ip), port_(port), conn_timeout_(timeout), send_timeout_(
                    send_timeout), recv_timeout_(recv_timeout), status_(false) {
                std::stringstream ss;
                ss << ip_ << ":" << port_;

                conn_name_ = ss.str();

                if (!connect()) { LOG2(forERROR, ERROR,
                                       "connection failed, (ip,port): " << ip << ", " << port);
                    return;
                }

                status_ = true;

                LOG2(forStat, INFO,
                     "connection success to (ip, port, timeout, send_timeout_, recv_timeout_): "
                     << ip << ", " << port << ", " << send_timeout_ << ", "
                     << recv_timeout_);
            }

            virtual ~MyConnection() {
                disconnect();
            }

            const std::string &getConnName() {
                return conn_name_;
            }

            T *operator->() const {
                return client_.get();
            }

            T &operator*() const {
                return *client_;
            }

            T *getClient() {
                return client_.get();
            }

            std::string getIp() {
                return ip_;
            }

            int getPort() {
                return port_;
            }

            bool status() {
                return status_;
            }

            // connect
            bool connect() {
                disconnect(); // disconnect first

                int num = 1;
                while (true) {
                    try {
                        boost::shared_ptr<TSocket> tsocket(new TSocket(ip_, port_));
                        tsocket->setConnTimeout(conn_timeout_);
                        tsocket->setSendTimeout(send_timeout_);
                        tsocket->setRecvTimeout(recv_timeout_);

                        transport_.reset(new TBufferedTransport(tsocket));
                        boost::shared_ptr<TProtocol> protocol(
                                new TBinaryProtocol(transport_));
                        client_.reset(new T(protocol));
                        transport_->open();

                        status_ = true;
                        break;
                    } catch (TException &tx) { LOG2(forERROR, ERROR,
                                                    "Init failed " << num << " times', exception: "
                                                    << tx.what());
                        status_ = false;
                        if (num++ > 2) {
                            break;
                        }
                    } catch (...) { LOG2(forERROR, ERROR, "Init failed catch an unknow exception.");
                        status_ = false;
                        break;
                    }
                }

                return status_;
            }

            // disconnect
            bool disconnect() {
                status_ = false;

                if (NULL != transport_) {
                    try {
                        transport_->close();
                    } catch (const apache::thrift::TException &tx) { LOG2(forERROR, ERROR,
                                                                          "Disconnect error : " << tx.what());
                        return false;
                    } catch (...) { LOG2(forERROR, ERROR, "Catch an unknow error.");
                        return false;
                    }
                } else {
                    return false;
                }

                return true;
            }
        };

    } // harpc
} // bfd

#endif /* CONNECT_POOL_H_ */
