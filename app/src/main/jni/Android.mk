LOCAL_PATH := $(call my-dir)
OPUS_PATH := $(LOCAL_PATH)/opus
include $(CLEAR_VARS)

LOCAL_MODULE    := opus

LOCAL_C_INCLUDES += $(OPUS_PATH)/include
LOCAL_C_INCLUDES += $(OPUS_PATH)/celt
LOCAL_C_INCLUDES += $(OPUS_PATH)/silk
LOCAL_C_INCLUDES += $(OPUS_PATH)/silk/fixed

LOCAL_CFLAGS := -DNULL=0 -DSOCKLEN_T=socklen_t -DLOCALE_NOT_USED -D_LARGEFILE_SOURCE=1 -D_FILE_OFFSET_BITS=64
LOCAL_CFLAGS        += -Drestrict='' -D__EMX__ -DOPUS_BUILD -DFIXED_POINT=1 -DDISABLE_FLOAT_API -DUSE_ALLOCA -DHAVE_LRINT -DHAVE_LRINTF  -DAVOID_TABLES
LOCAL_CFLAGS        +=  -w  -O3 -fno-strict-aliasing -fprefetch-loop-arrays  -fno-math-errno
LOCAL_CPPFLAGS      := -DBSD=1
LOCAL_CPPFLAGS      += -ffast-math -O3 -funroll-loops

LOCAL_LDLIBS := -llog
LOCAL_LDLIBS    += -latomic

OPUS_SRC := $(wildcard $(OPUS_PATH)/src/*.c)
CELT_SRC := $(wildcard $(OPUS_PATH)/celt/*.c)
SILK_SRC := $(wildcard $(OPUS_PATH)/silk/*.c)
SILK_SRC += $(wildcard $(OPUS_PATH)/silk/fixed/*.c)

LOCAL_SRC_FILES :=  $(OPUS_SRC:$(LOCAL_PATH)/%=%) \
                    $(CELT_SRC:$(LOCAL_PATH)/%=%) \
                    $(SILK_SRC:$(LOCAL_PATH)/%=%) \
                    com_example_codecdemo_JNIWrapper.cpp

include $(BUILD_SHARED_LIBRARY)