SET(EXECUTABLE_OUTPUT_PATH ${RESEARCH_BUILD_DIR} )
SET(LIBRARY_OUTPUT_PATH ${RESEARCH_BUILD_DIR} )

INCLUDE_DIRECTORIES(
	"${RESEARCH_ROOT_DIR}"
	"/usr/include/"
	"/usr/src/linux-headers-2.6.38-16/include"
	"/usr/local/include/"
	"/usr/lib/x86_64-linux-gnu/gcc/x86_64-linux-gnu/4.5/include"
	)

LINK_DIRECTORIES(
    "/usr/local/lib"
	"/usr/lib"
	"/usr/lib/x86_64-linux-gnu"
	)

LINK_LIBRARIES( pthread )

MACRO ( ADD_COMPONENT directoryname libraryname )
	AUX_SOURCE_DIRECTORY("${RESEARCH_ROOT_DIR}/${directoryname}" BUILD_${libraryname}_SRC_CPP_SOURCE )
	ADD_LIBRARY( ${libraryname} STATIC EXCLUDE_FROM_ALL ${BUILD_${libraryname}_SRC_CPP_SOURCE})
	#INCLUDE_DIRECTORIES(${RESEARCH_ROOT_DIR}/${directoryname})
	TARGET_LINK_LIBRARIES( ${libraryname} ${ARGN} )
ENDMACRO ( ADD_COMPONENT )

MACRO ( ADD_SERVICE directoryname libraryname )
	AUX_SOURCE_DIRECTORY("${RESEARCH_ROOT_DIR}/${directoryname}" BUILD_${libraryname}_SRC_CPP_SOURCE )
	ADD_LIBRARY( ${libraryname} SHARED EXCLUDE_FROM_ALL ${BUILD_${libraryname}_SRC_CPP_SOURCE})
	TARGET_LINK_LIBRARIES( ${libraryname} ${ARGN} )
ENDMACRO ( ADD_SERVICE )

MACRO ( ADD_SERVICE_ALL directoryname libraryname )
	AUX_SOURCE_DIRECTORY("${RESEARCH_ROOT_DIR}/${directoryname}" BUILD_${libraryname}_SRC_CPP_SOURCE )
	ADD_LIBRARY( ${libraryname} SHARED ${BUILD_${libraryname}_SRC_CPP_SOURCE} )
	TARGET_LINK_LIBRARIES( ${libraryname} ${ARGN} )
ENDMACRO ( ADD_SERVICE_ALL )

MACRO ( ADD_SERVICE_EXEC directoryname libraryname )
	MESSAGE(STATUS "DEBUG: ${ARGN}")
	AUX_SOURCE_DIRECTORY("${RESEARCH_ROOT_DIR}/${directoryname}" BUILD_${directoryname}_SRC_CPP_SOURCE )
	ADD_EXECUTABLE( ${libraryname} EXCLUDE_FROM_ALL ${BUILD_${directoryname}_SRC_CPP_SOURCE} )
	TARGET_LINK_LIBRARIES( ${libraryname} ${ARGN} )
ENDMACRO ( ADD_SERVICE_EXEC )

MACRO ( ADD_DC_SERVICE directoryname libraryname )
	AUX_SOURCE_DIRECTORY("${RESEARCH_ROOT_DIR}/${directoryname}" BUILD_${libraryname}_SRC_CPP_SOURCE )
	ADD_LIBRARY( ${libraryname} SHARED EXCLUDE_FROM_ALL ${BUILD_${libraryname}_SRC_CPP_SOURCE})
	TARGET_LINK_LIBRARIES( ${libraryname} ${ARGN} )
ENDMACRO ( ADD_DC_SERVICE )

MACRO ( ADD_DC_SERVICE_EXEC directoryname libraryname )
    MESSAGE( STATUS "DEBUG: directoryname ${directoryname}")    
	MESSAGE(STATUS "DEBUG: ${ARGN}")
	AUX_SOURCE_DIRECTORY("${RESEARCH_ROOT_DIR}/${directoryname}" BUILD_${directoryname}_SRC_CPP_SOURCE )
	MESSAGE( STATUS "DEBUG: BUILD_${directoryname}_SRC_CPP_SOURCE -------------- ${BUILD_${directoryname}_SRC_CPP_SOURCE}" )
	ADD_EXECUTABLE( ${libraryname} EXCLUDE_FROM_ALL ${BUILD_${directoryname}_SRC_CPP_SOURCE})
	TARGET_LINK_LIBRARIES( ${libraryname} ${ARGN} )
ENDMACRO ( ADD_DC_SERVICE_EXEC )


