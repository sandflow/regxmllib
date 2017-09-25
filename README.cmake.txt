1 cmake options
-DBUILD_SHARED_LIB=(OFF|ON) default: OFF
-DCMAKE_BUILD_TYPE=(release|debug) default:release

2 build options
make install or Visual Studio target INSTALL will install regxmllib

3 using regxmllib in other applications
add
find_package(regxmllibc)
target_link_libraries(${EXECUTABLE} general regxmllibc)
to application's CMakeLists.txt


