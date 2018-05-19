cmake_minimum_required (VERSION 3.5)
project (regxmllibc)

include(FindXercesC)
find_package(XercesC REQUIRED)
include_directories( ${XercesC_INCLUDE_DIR}
						src/main/cpp
					)


file(GLOB_RECURSE SRC_FILES src/main/cpp/*.cpp src/main/cpp/*.h )
add_library(${PROJECT_NAME} ${SRC_FILES})
install(TARGETS ${PROJECT_NAME} LIBRARY DESTINATION lib ARCHIVE DESTINATION lib)
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

#install

install(DIRECTORY src/main/cpp/com DESTINATION include FILES_MATCHING PATTERN "*.h")
