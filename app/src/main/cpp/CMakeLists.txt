cmake_minimum_required(VERSION 3.22.1)

project("antidpi")

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra -O3 -fPIC")

add_library(
    antidpi
    SHARED
    ciadpi/desync.c
    ciadpi/conntrack.c
    ciadpi/proxy.c
    tun2socks/tun2socks.c
    jni_bridge.c
)

find_library(log-lib log)

target_include_directories(
    antidpi
    PRIVATE
    ciadpi
    tun2socks
)

target_link_libraries(
    antidpi
    ${log-lib}
)
