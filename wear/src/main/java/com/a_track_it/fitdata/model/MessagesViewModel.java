package com.a_track_it.fitdata.model;

import android.util.Log;

import java.util.LinkedList;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MessagesViewModel extends ViewModel {
    private LinkedList<String> messageQueue = new LinkedList<>();  // FIFO queue of messages for display on HomePage and LiveFragments
    private LinkedList<String> messageQueue2 = new LinkedList<>();

    private MutableLiveData<String> currentMsg = new MutableLiveData<>();
    private MutableLiveData<String> otherMsg = new MutableLiveData<>();
    private MutableLiveData<String> locationMsg = new MutableLiveData<>();
    private MutableLiveData<String> bpmMsg = new MutableLiveData<>();
    private MutableLiveData<String> stepsMsg = new MutableLiveData<>();

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(MessagesViewModel.class.getSimpleName(), "onCleared" );
    }

    public MessagesViewModel() {
        super();
    }

    public MutableLiveData<String> getRestMsg() {
        return restMsg;
    }
    public void setRestMsg(String sMsg){
        restMsg.postValue(sMsg);
    }
    private MutableLiveData<String> restMsg = new MutableLiveData<>();

    private static final String EMPTY_STRING = "";

    public MutableLiveData<String> CurrentMessage(){
        return currentMsg;
    }
    public MutableLiveData<String> OtherMessage() { return otherMsg; }

    public void addMessage(String msg){
        messageQueue.addLast(msg);
    }

    public void addOtherMessage(String msg){
        messageQueue2.addLast(msg);
    }

    public String getMessage(){
        String sRet;
        if (messageQueue.size() > 1){ sRet = messageQueue.getFirst();} else {sRet = EMPTY_STRING;}
        return sRet;
    }

    public String getOtherMessage(){
        String sRet;
        if (messageQueue2.size() > 1){ sRet = messageQueue2.getFirst();} else {sRet = EMPTY_STRING;}
        return sRet;
    }
    public void getNextMessage(){

        if (messageQueue.size() > 1){
            messageQueue.removeFirst();
            currentMsg.setValue(messageQueue.getFirst());
        }else
            currentMsg.setValue(EMPTY_STRING);
    }
    public void getNextOtherMessage(){

        if (messageQueue2.size() > 1){
            messageQueue2.removeFirst();
            otherMsg.setValue(messageQueue2.getFirst());
        }else
            otherMsg.setValue(EMPTY_STRING);
    }
    public int getMessageCount(){
        return messageQueue.size();
    }

    public int getOtherMessageCount(){
        return messageQueue2.size();
    }

    public MutableLiveData<String> getLocationMsg() {
        return locationMsg;
    }
    public void addLocationMsg(String sMsg){ this.locationMsg.postValue(sMsg);}

    public MutableLiveData<String> getBpmMsg() {
        return bpmMsg;
    }
    public void addBpmMsg(String sMsg){ this.bpmMsg.postValue(sMsg);}

    public MutableLiveData<String> getStepsMsg() {
        return stepsMsg;
    }
    public void addStepsMsg(String sMsg){ this.stepsMsg.postValue(sMsg);}
}
