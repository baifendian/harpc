cmake_minimum_required(VERSION 2.8)

PROJECT( DemoClient )

SET(CMAKE_VERBOSE_MAKEFILE "1")

SET (CMAKE_CXX_COMPILER "g++")

# -std=c++0x
SET (CMAKE_CXX_FLAGS "-std=c++0x -fshort-wchar -O2 -fPIC -Wall -DNEWARCH -DPOSIX -DPOSIX -DMONGO_HAVE_STDINT -DHAVE_NETINET_IN_H -D__STDC_LIMIT_MACROS -DNDEBUG")
SET (CMAKE_C_FLAGS "-std=c++0x -fshort-wchar -O2 -fPIC -frepo -Wall -DNEWARCH -DPOSIX -DMONGO_HAVE_STDINT -D__STDC_LIMIT_MACROS -DNDEBUG")

#
# The out-of-source building directory
#
INCLUDE( "../cmake/start.cmake" )
