# QuJing

[中文版](./README.md)

QuJing is an Xposed module that allows you to dynamically monitor (hook) function calls, view stack information, and perform reflective invocations on Android devices through a PC browser. It eliminates the need for writing frequent hook code and provides a visual interface that is more user-friendly, especially for beginners.

## Features

1. Enumerate all apps on an Android device.
2. Search for methods based on class name and method name.
3. Method monitoring with printing of call stacks and input/output parameters.
4. Forced execution of target methods.

## Known Issues

1. When functions are frequently called, a large amount of stack information and input/output parameters are printed on the browser page, which may cause the page to be overloaded. If unnecessary data is present, refreshing the page can resolve the issue.
2. Some layout issues may occur in the frontend display.
3. Does not support scenarios where hooking immediately after booting (as the target app needs to be set after each boot).
4. Does not support apps that do not have network permissions.
5. ~~There seems to be an issue with LSPOSED support; the symptom is that port 61000 cannot be opened. It will be supported later~~ (LSPOSED is now supported. When using, make sure to not only select the target app in Lsposed, **but also check "System Framework" [as shown here](https://github.com/Mocha-L/QuJing/assets/24688287/53906a4e-96ca-4824-83c5-eb5cc70dd2af)~~).

## Usage

You can directly download the APK file from the "apk" directory and install it for testing. For detailed usage instructions, please refer to [this article](https://mp.weixin.qq.com/s/zXRKNximCk5DDFfFZcxDJQ).

If it helps you, don't forget to give it a star.

## Demo

Enumerating all apps on the phone for selection.

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E9%85%8D%E7%BD%AE%E7%9B%AE%E6%A0%87%E5%BA%94%E7%94%A8.png" width="1000px" />

Guiding manual operations on the selected app.

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%89%8B%E5%8A%A8%E6%93%8D%E4%BD%9C%E6%8C%87%E5%AF%BC.png" width="1000px" />

Enter the app, search for classes and methods to monitor.

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%90%9C%E7%B4%A2%E7%9B%AE%E6%A0%87%E7%B1%BB-%E6%96%B9%E6%B3%95.png" width="1000px" />

Enter the monitored method, view basic information and call stack, and print input/output parameters.

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E7%9B%91%E6%8E%A7%E6%96%B9%E6%B3%95.png" width="1000px" />
<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E7%9B%91%E6%8E%A7%E6%96%B9%E6%B3%

952.png" width="1000px" />

Supported types for method execution.

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%89%A7%E8%A1%8C%E6%96%B9%E6%B3%95.png" width="1000px" />


## Issue Discussion

If you have any questions, feel free to raise an issue. You are also welcome to contribute to further optimizations and submit pull requests.

You can also join my knowledge planet "爬虫三十六计" for further discussions.

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%98%9F%E7%90%83.png" width="400px" />

## Acknowledgements

This project is based on [xserver](https://github.com/monkeylord/XServer) and optimized accordingly. Thanks to the original author.

Thanks to [@小黄鸭爱学习](https://github.com/HuRuWo) for helping with optimizations and resolving classloader errors in certain cases.