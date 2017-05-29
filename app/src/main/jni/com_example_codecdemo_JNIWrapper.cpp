#include "com_example_codecdemo_JNIWrapper.h"
#include "opus.h"

#define OPUS_SAMPLE_RATE 8000
#define OPUS_CHANNEL_COUNT 1
#define OPUS_FRAME_SIZE 160

OpusEncoder *opus_encoder = NULL;
OpusDecoder *opus_decoder = NULL;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK){
		return -1;
	}

	return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *, void *) {

}

void initOpusEncoder(){
    int error;
    opus_encoder = opus_encoder_create(OPUS_SAMPLE_RATE,OPUS_CHANNEL_COUNT,OPUS_APPLICATION_VOIP,&error);
}

void dinitOpusEncoder(){
    if(opus_encoder != NULL){
        opus_encoder_destroy(opus_encoder);
    }
}

void initOpusDecoder(){
    int error;
    opus_decoder = opus_decoder_create(OPUS_SAMPLE_RATE,OPUS_CHANNEL_COUNT,&error);
}

void dinitOpusDecoder(){
    if(opus_decoder != NULL){
        opus_decoder_destroy(opus_decoder);
    }
}

JNIEXPORT void JNICALL Java_com_example_codecdemo_JNIWrapper_initCodec
  (JNIEnv *env, jclass obj)
{
    initOpusEncoder();
    initOpusDecoder();
}

JNIEXPORT void JNICALL Java_com_example_codecdemo_JNIWrapper_deInitCodec
  (JNIEnv *env, jclass obj)
{
    dinitOpusEncoder();
    dinitOpusDecoder();
}

JNIEXPORT jbyteArray JNICALL Java_com_example_codecdemo_JNIWrapper_encodeOpus
(JNIEnv *env, jclass obj, jshortArray in)
{
    int len = env->GetArrayLength(in);
	short *data = (short*) env->GetShortArrayElements(in, 0);
	unsigned char out[4000];
    int out_len = opus_encode(opus_encoder,data,OPUS_FRAME_SIZE,out,4000);
    jbyteArray ret = env->NewByteArray(out_len);
    env->SetByteArrayRegion(ret, 0, out_len, (jbyte *)out);
    env->ReleaseShortArrayElements(in, (jshort*)data, JNI_FALSE);
    return ret;
}

JNIEXPORT jshortArray JNICALL Java_com_example_codecdemo_JNIWrapper_decodeOpus
(JNIEnv *env, jclass obj, jbyteArray in,int len){
    unsigned char *data = (unsigned char*) env->GetByteArrayElements(in, 0);
    short out[4000];
    int out_len = opus_decode( opus_decoder, data, len, out, OPUS_FRAME_SIZE, 0);
    jshortArray ret = env->NewShortArray(out_len);
    env->SetShortArrayRegion(ret, 0, out_len, (jshort *)out);
    env->ReleaseByteArrayElements(in, (jbyte*)data, JNI_FALSE);
    return ret;
}