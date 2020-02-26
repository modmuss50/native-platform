#pragma once

#ifdef _WIN32

#include <Shlwapi.h>
#include <string>
#include <unordered_map>
#include <wchar.h>
#include <windows.h>

// Needs to stay below <windows.h> otherwise byte symbol gets confused with std::byte
#include "generic_fsnotifier.h"
#include "net_rubygrapefruit_platform_internal_jni_WindowsFileEventFunctions.h"
#include "net_rubygrapefruit_platform_internal_jni_WindowsFileEventFunctions_WatcherImpl.h"

using namespace std;

#define EVENT_BUFFER_SIZE (16 * 1024)

#define CREATE_SHARE (FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE)
#define CREATE_FLAGS (FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OVERLAPPED)

#define EVENT_MASK (FILE_NOTIFY_CHANGE_FILE_NAME | FILE_NOTIFY_CHANGE_DIR_NAME | FILE_NOTIFY_CHANGE_ATTRIBUTES | FILE_NOTIFY_CHANGE_SIZE | FILE_NOTIFY_CHANGE_LAST_WRITE)

class Server;
class WatchPoint;

#define WATCH_LISTENING 1
#define WATCH_NOT_LISTENING 2
#define WATCH_FINISHED 3
#define WATCH_UNINITIALIZED -1
#define WATCH_FAILED_TO_LISTEN -2

class WatchPoint {
public:
    WatchPoint(Server* server, const u16string& path, HANDLE directoryHandle, HANDLE serverThreadHandle);
    ~WatchPoint();
    void close();
    void listen();
    int awaitListeningStarted(HANDLE threadHandle);
    int status;

private:
    Server* server;
    u16string path;
    friend class Server;
    HANDLE directoryHandle;
    OVERLAPPED overlapped;
    FILE_NOTIFY_INFORMATION* buffer;

    mutex listenerMutex;
    condition_variable listenerStarted;

    void handleEvent(DWORD errorCode, DWORD bytesTransferred);
    void handlePathChanged(FILE_NOTIFY_INFORMATION* info);
    friend static void CALLBACK handleEventCallback(DWORD errorCode, DWORD bytesTransferred, LPOVERLAPPED overlapped);
};

class Server : public AbstractServer {
public:
    Server(JNIEnv* env, jobject watcherCallback);
    ~Server();

    void startWatching(JNIEnv* env, const u16string& path);
    void stopWatching(JNIEnv* env, const u16string& path);

    void reportEvent(jint type, const u16string& changedPath);
    void reportFinished(const WatchPoint& watchPoint);

protected:
    void Server::runLoop(JNIEnv* env, function<void(exception_ptr)> notifyStarted) override;

private:
    unordered_map<u16string, WatchPoint> watchPoints;

    bool terminate = false;

    friend static void CALLBACK requestTerminationCallback(_In_ ULONG_PTR arg);
    void requestTermination();
};

#endif
