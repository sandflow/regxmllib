cmake_minimum_required (VERSION 3.6)
project (regxmllibc)
option(BUILD_SHARED_LIB Bool OFF)

# Set build type
if(BUILD_SHARED_LIB)
set(LIB_TYPE SHARED)
if(WIN32 AND NOT CYGWIN)
set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS TRUE)
endif(WIN32 AND NOT CYGWIN)
else(BUILD_SHARED_LIB)
set(LIB_TYPE STATIC)
add_definitions( -DUTILS_LIB_STATIC )
endif(BUILD_SHARED_LIB)
message(STATUS "*** LIB_TYPE is ${LIB_TYPE}")

include(FindXercesC)
find_package(XercesC REQUIRED)
include_directories( ${XercesC_INCLUDE_DIR}
						src/main/cpp
					)

# Append "_d" if debug lib.
set(CMAKE_DEBUG_POSTFIX _d) 

file(GLOB_RECURSE SRC_FILES src/main/cpp/*.cpp )
file(GLOB_RECURSE INC_FILES src/main/cpp/*.h )
add_library(${PROJECT_NAME} ${LIB_TYPE} ${SRC_FILES} ${INC_FILES})

set_property(TARGET ${PROJECT_NAME} PROPERTY CXX_STANDARD 98)

target_link_libraries ( ${PROJECT_NAME} ${XercesC_LIBRARY} )

#  Set Visual Studio filters for nested sourcce paths
foreach(source IN LISTS SRC_FILES INC_FILES)
	file(RELATIVE_PATH rel_source ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/com/ ${source})
    get_filename_component(source_path ${rel_source} DIRECTORY)
    string(REPLACE "\\" "/" source_path_msvc "${source_path}")
    source_group("${source_path_msvc}" FILES "${source}")
endforeach()

if(WIN32 AND NOT CYGWIN)
  set(DEF_INSTALL_CMAKE_DIR CMake)
else()
  set(DEF_INSTALL_CMAKE_DIR lib/CMake/${PROJECT_NAME})
endif()
set(INSTALL_CMAKE_DIR ${DEF_INSTALL_CMAKE_DIR} CACHE PATH
  "Installation directory for CMake files")
set(INSTALL_LIB_DIR lib CACHE PATH "Installation directory for libraries")
set(INSTALL_INCLUDE_DIR include CACHE PATH "Installation directory for header files")

# Make relative paths absolute (needed later on)
foreach(p LIB BIN INCLUDE CMAKE)
  set(var INSTALL_${p}_DIR)
  if(NOT IS_ABSOLUTE "${${var}}")
    set(${var} "${CMAKE_INSTALL_PREFIX}/${${var}}")
  endif()
endforeach()

# Export the package for use from the build-tree
# (this registers the build-tree with a global CMake-registry)
export(PACKAGE ${PROJECT_NAME})
 
# Create the ${PROJECT_NAME}Config.cmake file in the install tree
file(RELATIVE_PATH REL_INCLUDE_DIR "${INSTALL_CMAKE_DIR}" "${INSTALL_INCLUDE_DIR}")
set(CONF_INCLUDE_DIRS "\${REGXMLLIBC_CMAKE_DIR}/${REL_INCLUDE_DIR}")
configure_file(${PROJECT_NAME}Config.cmake.in
  "${PROJECT_BINARY_DIR}${CMAKE_FILES_DIRECTORY}/${PROJECT_NAME}Config.cmake" @ONLY)


install(EXPORT ${PROJECT_NAME}Targets DESTINATION
  ${INSTALL_CMAKE_DIR} COMPONENT dev)

install(TARGETS ${PROJECT_NAME} 
EXPORT ${PROJECT_NAME}Targets DESTINATION ${INSTALL_LIB_DIR}
)

foreach ( file ${INC_FILES} )
    get_filename_component( dir ${file} DIRECTORY )
    file(RELATIVE_PATH dir2 "${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp" ${dir})
    message(STATUS ${dir})
    message(STATUS ${dir2})
    install( FILES ${file} DESTINATION "${INSTALL_INCLUDE_DIR}/${PROJECT_NAME}/${dir2}" )
endforeach()


install(FILES
  "${PROJECT_BINARY_DIR}${CMAKE_FILES_DIRECTORY}/${PROJECT_NAME}Config.cmake"
  DESTINATION "${INSTALL_CMAKE_DIR}" COMPONENT dev)


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
