cmake_minimum_required (VERSION 3.5)
project (regxmllibc)

include(FindXercesC)
find_package(XercesC REQUIRED)
include_directories( ${XercesC_INCLUDE_DIR}
						src/main/cpp
					)


file(GLOB_RECURSE SRC_FILES src/main/cpp/*.cpp src/main/cpp/*.h )
add_library(${PROJECT_NAME} SHARED ${SRC_FILES})
install(TARGETS ${PROJECT_NAME} LIBRARY DESTINATION lib)
set_property(TARGET ${PROJECT_NAME} PROPERTY CXX_STANDARD 98)



target_link_libraries ( ${PROJECT_NAME} ${XercesC_LIBRARY} )

foreach(source IN LISTS SRC_FILES)
	file(RELATIVE_PATH rel_source ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/com/ ${source})
    get_filename_component(source_path ${rel_source} DIRECTORY)
    string(REPLACE "\\" "/" source_path_msvc "${source_path}")
    source_group("${source_path_msvc}" FILES "${source}")
endforeach()



#unit tests

enable_testing()

file( GLOB_RECURSE UNIT_TESTS src/test/cpp/*.cpp )
file(COPY "src/test/resources" DESTINATION "${CMAKE_BINARY_DIR}")

foreach( UNIT_TEST_PATH ${UNIT_TESTS} )
	get_filename_component(UNIT_TEST_NAME ${UNIT_TEST_PATH} NAME_WE)

    add_executable(${UNIT_TEST_NAME} ${UNIT_TEST_PATH} )

    target_link_libraries( ${UNIT_TEST_NAME} regxmllibc ${XercesC_LIBRARY})
	add_test(${UNIT_TEST_NAME}  ${UNIT_TEST_NAME} WORKINGDIRECTORY "${CMAKE_BINARY_DIR}")
endforeach( UNIT_TEST_PATH ${UNIT_TESTS} )

#add_executable(AUIDTest src/test/cpp/com/sandflow/smpte/util/AUIDTest.cpp)
#target_link_libraries(AUIDTest regxmllibc)
#add_test(AUIDTest AUIDTest)

file( GLOB_RECURSE HEADER_FILE_LIST ${CMAKE_SOURCE_DIR}/src/main/cpp/com/*.h )
foreach(HEADER_FILE_PATH ${HEADER_FILE_LIST} )
    file(RELATIVE_PATH relative_header_path ${CMAKE_SOURCE_DIR}/src/main/cpp/ ${HEADER_FILE_PATH})
    get_filename_component(relative_path ${relative_header_path} DIRECTORY)
    install(FILES ${HEADER_FILE_PATH} DESTINATION include/${relative_path})
endforeach(HEADER_FILE_PATH ${HEADER_FILE_LIST} )

file( GLOB_RECURSE SRC_RESOURCE_LIST ${CMAKE_SOURCE_DIR}/src/main/resources/*)
foreach(SRC_RESOURCE_PATH ${SRC_RESOURCE_LIST} )
    file(RELATIVE_PATH relative_src_res_path ${CMAKE_SOURCE_DIR}/.. ${SRC_RESOURCE_PATH})
    get_filename_component(relative_path ${relative_src_res_path} DIRECTORY)
    install(FILES ${SRC_RESOURCE_PATH} DESTINATION res/${relative_path})
endforeach(SRC_RESOURCE_PATH ${SRC_RESOURCE_LIST} )
