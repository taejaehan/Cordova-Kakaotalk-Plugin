#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>

@interface KakaoTalk : CDVPlugin

- (void) login:(CDVInvokedUrlCommand*)command;
- (void) logout:(CDVInvokedUrlCommand*)command;
- (void) share:(CDVInvokedUrlCommand*)command;
@end

