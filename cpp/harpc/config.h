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
#ifndef CONFIG_H_
#define CONFIG_H_

#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/shared_ptr.hpp>
#include <string>

using namespace std;
using namespace boost::property_tree;
using namespace boost::property_tree::xml_parser;

namespace bfd {
    namespace harpc {
        // 解析 xml 配置文件，使用了 boost 的解析库
        inline const ptree getPTree(const std::string &filename) {
            ptree pt_tmp;
            read_xml(filename, pt_tmp);

            return pt_tmp;
        }

        class Configuration {
        private:
            ptree pt_;

        public:
            Configuration() {
            }

            void init(const std::string &filename) {
                // Load the XML file into the property tree. If reading fails
                // (cannot open file, parse error), an exception is thrown.
                read_xml(filename, pt_);
            }

            const ptree getPTree() {
                return pt_;
            }

            const ptree getPTree(const std::string &filename) {
                ptree pt_tmp;
                read_xml(filename, pt_tmp);

                return pt_tmp;
            }
        };

        typedef boost::shared_ptr<Configuration> ConfigurationPtr;

        class ConfigFactory {
        public:
            static ConfigurationPtr getConfigInstance() {
                static ConfigurationPtr ConfigPtr;
                if (!ConfigPtr) {
                    ConfigPtr.reset(new Configuration());
                }
                return ConfigPtr;
            }
        };

    } // harpc
} // bfd

#endif /* CONFIG_H_ */
