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
#ifndef THRIFT_CLIENT_POOL_H_
#define THRIFT_CLIENT_POOL_H_

#include <vector>
#include <map>
#include <set>
#include <utility>
#include <string>
#include <ctime>
#include <boost/algorithm/string.hpp>
#include <boost/thread/mutex.hpp>
#include "myconnect.h"
#include "config.h"

using namespace ::boost::property_tree;

#define LOG2(a, b, c) ;

namespace bfd {
    namespace  harpc {

        template<typename T>
        class LocalPool {
        private:
            std::deque<T *> avaliable_; // 维护的是一个没有使用的队列，可用队列

        public:
            LocalPool() {
            }

            virtual ~LocalPool() {
            }

            /**
             * borrow an item from pool
             */
            T *borrowItem() {
                T *rt = NULL;

                if (!avaliable_.empty()) {
                    rt = avaliable_.front();
                    avaliable_.pop_front();
                }

                return rt;
            }

            /**
             * @item: return an item to the pool
             */
            void returnItem(T *item) {
                avaliable_.push_back(item);
            }

            /**
             *
             */
            bool isEmpty() {
                return avaliable_.empty();
            }

            /**
             * get the unused num of client
             */
            size_t getUnusedNum() {
                return avaliable_.size();
            }
        };

        template<typename T>
        class ThriftClientPool {
        private:
            LocalPool<MyConnection<T> > bad_conn_pool_; // the bad ip-port pair
            LocalPool<MyConnection<T> > conn_pool_; // connect pool

            int init_pool_size_; // the init size of connect pool
            int max_pool_size_; // the max pool size
            int cur_pool_size_; // cur pool size

            int check_interval_; // check interval for bad ip-port, judge if is ok now
            int check_noopen_interval_;

            int conn_timeout_; // conntect timeout
            int send_timeout_; // send timeout
            int recv_timeout_; // recv timeout

            std::vector<std::pair<std::string, int> > conn_address_; // connection address

            long last_check_; // last check time
            long last_noopen_check_; // last not open check

            boost::mutex poolMutex_; // pool mutex

        public:
            ThriftClientPool(const std::string &config_file) {
                // init configuration
                //utils::ConfigFactory::getConfigInstance()->init(config_file);
                ptree pt = bfd::harpc::getPTree(config_file);

                init_pool_size_ = pt.get<int>("configuration.threshold.initPoolSize");
                max_pool_size_ = pt.get<int>("configuration.threshold.maxPoolSize");
                cur_pool_size_ = init_pool_size_;

                check_interval_ = pt.get<int>("configuration.threshold.checkInterval");
                check_noopen_interval_ = pt.get<int>(
                        "configuration.threshold.checkNoOpenInterval");

                conn_timeout_ = pt.get<int>("configuration.thrift.connTimeOut");
                send_timeout_ = pt.get<int>("configuration.thrift.sendTimeOut");
                recv_timeout_ = pt.get<int>("configuration.thrift.recvTimeOut");

                std::string conn_addr = pt.get<std::string>(
                        "configuration.thrift.connect");

                LOG2(forStat, INFO,
                     "pool size: " << init_pool_size_ << "\tcur pool size: "
                     << cur_pool_size_ << "\tmax pool size: "
                     << max_pool_size_ << "\tcheck interval: "
                     << check_interval_ << "\tcheck noopen interval: "
                     << check_noopen_interval_);

                LOG2(forStat, INFO,
                     "\tconn timeout: " << conn_timeout_ << "\tsend timeout: "
                     << send_timeout_ << "\trecv timeout: " << recv_timeout_
                     << "\tconn addr: " << conn_addr);

                std::vector<std::string> strs;
                boost::split(strs, conn_addr, boost::is_any_of(","));

                // iterator the strs
                // for (const auto &addr : strs) {
                for (std::vector<std::string>::const_iterator iter = strs.begin();
                     iter != strs.end(); ++iter) {
                    size_t index = iter->find(":");

                    // find the index
                    if (index <= 0) {
                        break;
                    }

                    // ip & port
                    std::string ip = iter->substr(0, index);
                    int port = atoi(iter->substr(index + 1).c_str());

                    conn_address_.push_back(std::pair<std::string, int>(ip, port));

                    // create conn
                    for (int i = 0; i < init_pool_size_; ++i) {
                        createConn(ip, port);
                    }
                }

                last_check_ = time(NULL);
                last_noopen_check_ = time(NULL);
            }

            virtual ~ThriftClientPool() {
                releaseRes();
            }

            // @param addSimi: if this item is exception, then the simi connection is exception too.
            void returnBadItem(MyConnection<T> *item, bool addSimi) {
                if (item == NULL) {
                    return;
                }

                boost::mutex::scoped_lock lock(poolMutex_);

                const std::string &name = item->getConnName();

                // disconnect first
                item->disconnect();

                bad_conn_pool_.returnItem(item);

                LOG2(forStat, ERROR, "return Bad Item: " << name);

                if (addSimi) {
                    MyConnection<T> *t = conn_pool_.borrowItem();
                    size_t sizeLen = conn_pool_.getUnusedNum();

                    // for statement
                    for (size_t i = 0; t; ++i) {
                        if (t->getConnName() == name) {
                            // disconnect first
                            t->disconnect();

                            bad_conn_pool_.returnItem(t);

                            LOG2(forStat, ERROR, "return Bad Item: " << name);
                        } else {
                            conn_pool_.returnItem(t);
                        }

                        // if i is more than sizeLen, then break
                        if (i >= sizeLen) {
                            break;
                        }

                        t = conn_pool_.borrowItem();
                    }
                }
            }

            // get the thrift client, if fork not alived, then
            // close the connection and reconnect.
            MyConnection<T> *borrowItem() {
                boost::mutex::scoped_lock lock(poolMutex_);

                MyConnection<T> *t = conn_pool_.borrowItem();

                // time is ok
                long now = time(NULL);

                // check if need reconnect
                if (!bad_conn_pool_.isEmpty()
                    && (now - last_check_ >= check_interval_ || !t)) { LOG2(forStat, INFO,
                                                                            "begin check bad conn pool...");

                    size_t sizeLen = bad_conn_pool_.getUnusedNum();
                    MyConnection<T> *t2 = bad_conn_pool_.borrowItem();

                    // for statement
                    std::set<std::string> ip_port;
                    for (size_t i = 0; t2; ++i) {
                        std::string testConn = t2->getConnName();

                        // if not find in bad conn, and connect is ok
                        if (ip_port.find(testConn) == ip_port.end() && t2->connect()) {
                            conn_pool_.returnItem(t2);

                            LOG2(forStat, INFO, "recovery Item: " << testConn);
                        } else {
                            ip_port.insert(testConn);

                            bad_conn_pool_.returnItem(t2);LOG2(forStat, ERROR,
                                                               "still not recovery Item: " << testConn);
                        }

                        // if i is more than sizeLen, then break
                        if (i >= sizeLen) {
                            break;
                        }

                        t2 = bad_conn_pool_.borrowItem();
                    }

                    if (!t) {
                        t = conn_pool_.borrowItem();
                    }

                    last_check_ = now;

                    LOG2(forStat, WARN, "end check bad conn pool.");
                }

                // pool size is not satisfier
                if (!t && bad_conn_pool_.isEmpty()
                    && cur_pool_size_ <= max_pool_size_) {
                    cur_pool_size_ += init_pool_size_;

                    LOG2(forStat, WARN,
                         "increase the pool size, cur pool size is: "
                         << cur_pool_size_);

                    // begin init
                    // for (const auto &addr : conn_address_) {
                    for (std::vector<std::pair<std::string, int> >::const_iterator iter =
                            conn_address_.begin(); iter != conn_address_.end();
                         ++iter) {
                        std::string ip = iter->first;
                        int port = iter->second;

                        // create conn
                        for (int i = 0; i < init_pool_size_; ++i) {
                            createConn(ip, port);
                        }
                    }

                    t = conn_pool_.borrowItem();
                }

                return t;
            }

            /**
             * @item: return an item to the pool
             */
            void returnItem(MyConnection<T> *item) {
                if (item == NULL) {
                    return;
                }

                boost::mutex::scoped_lock lock(poolMutex_);
                conn_pool_.returnItem(item);
            }

            /**
             * @item: run functor with the ptr,
             */
            template<typename OpeFunctor, typename... ArgTypes>
            bool invoke(OpeFunctor ope, ArgTypes... arg) {
                MyConnection<T> *t = borrowItem();

                if (NULL == t) { LOG2(forStat, WARN, "thrift pool is empty");
                    return false;
                }

                bool ret = false;

                try {
                    ope(t->getClient(), arg...);
                    returnItem(t);
                    ret = true;
                } catch (const apache::thrift::transport::TTransportException &tx) {
                    if (tx.getType()
                        == apache::thrift::transport::TTransportException::TIMED_OUT) { LOG2(forStat, WARN,
                                                                                             "thrift time out.");
                        returnBadItem(t, false);
                    } else { LOG2(forStat, WARN, "thrift error.text:" << tx.what());
                        returnBadItem(t, true);
                    }
                } catch (const apache::thrift::TException &tx) { LOG2(forStat, WARN, "thrift error.text:" << tx.what());
                    returnBadItem(t, false);
                } catch (std::exception &ex) { LOG2(forStat, WARN, "thrift error.text:" << ex.what());
                    returnBadItem(t, false);
                } catch (...) { LOG2(forStat, WARN, "thrift error.unknown error.");
                    returnBadItem(t, false);
                }

                return ret;
            }

            /*
            template<typename OpeFunctor, typename... ArgTypes>
            bool customOpe(OpeFunctor ope, ArgTypes... arg) {
                MyConnection<T> *t = borrowItem();

                if (NULL == t) { LOG2(forStat, WARN, "thrift pool is empty");
                    return false;
                }

                bool ret = false;

                try {
                    ope(t->getClient(), arg...);
                    returnItem(t);
                    ret = true;
                } catch (const apache::thrift::transport::TTransportException &tx) {
                    if (tx.getType()
                        == apache::thrift::transport::TTransportException::TIMED_OUT) {
                        returnBadItem(t, false);
                    } else {
                        returnBadItem(t, true);
                    }
                } catch (const apache::thrift::TException &tx) {
                    returnBadItem(t, false);
                } catch (std::exception &ex) {
                    returnBadItem(t, false);
                } catch (...) {
                    returnBadItem(t, false);
                }

                return ret;
            }
            */

        private:
            int createConn(const std::string &ip, int port) {
                LOG2(forStat, WARN, "before create conn: " << ip << "\t" << port);

                MyConnection<T> *nt = new MyConnection<T>(ip, port, conn_timeout_,
                                                          send_timeout_, recv_timeout_);

                if (nt->status()) {
                    conn_pool_.returnItem(nt);LOG2(forStat, WARN, "create conn success: " << ip << "\t" << port);

                    return 0;
                } else {
                    bad_conn_pool_.returnItem(nt);LOG2(forStat, ERROR, "create conn failed." << ip << ":" << port);
                }

                return 1;
            }

            void releaseRes() {
                boost::mutex::scoped_lock lock(poolMutex_);

                MyConnection<T> *t = conn_pool_.borrowItem();
                while (t) {
                    delete t;
                    t = conn_pool_.borrowItem();
                }

                t = bad_conn_pool_.borrowItem();
                while (t) {
                    delete t;
                    t = bad_conn_pool_.borrowItem();
                }
            }
        };

    } // harpc
} // bfd

#endif
