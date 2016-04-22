#import "KakaoTalk.h"
#import <Cordova/CDVPlugin.h>
#import <KakaoOpenSDK/KakaoOpenSDK.h>

@implementation KakaoTalk

- (void) login:(CDVInvokedUrlCommand*) command
{
    [[KOSession sharedSession] close];
    
    [[KOSession sharedSession] openWithCompletionHandler:^(NSError *error) {
        
        if ([[KOSession sharedSession] isOpen]) {
            // login success
            NSLog(@"login succeeded.");
            [KOSessionTask meTaskWithCompletionHandler:^(KOUser* result, NSError *error) {
                CDVPluginResult* pluginResult = nil;
                if (result) {
                    // success
                    NSLog(@"userId=%@", result.ID);
                    NSLog(@"nickName=%@", [result propertyForKey:@"nickname"]);
                    NSLog(@"profileImage=%@", [result propertyForKey:@"profile_image"]);
                    
                    NSDictionary *userSession = @{
                                          @"id": result.ID,
                                          @"nickname": [result propertyForKey:@"nickname"],
                                          @"profile_image": [result propertyForKey:@"profile_image"]};
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userSession];
                } else {
                    // failed
                    NSLog(@"login session failed.");
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
                }
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }];
        } else {
            // failed
            NSLog(@"login failed.");
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
        
        
    } authParams:nil authType:(KOAuthType)KOAuthTypeTalk, nil];
}

- (void)logout:(CDVInvokedUrlCommand*)command
{
    [[KOSession sharedSession] logoutAndCloseWithCompletionHandler:^(BOOL success, NSError *error) {
        if (success) {
            // logout success.
            NSLog(@"Successful logout.");
        } else {
            // failed
            NSLog(@"failed to logout.");
        }
    }];
    CDVPluginResult* pluginResult = pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)share:(CDVInvokedUrlCommand *)command
{
        NSMutableArray *kakaoArray = [NSMutableArray array];

        NSMutableDictionary *options = [[command.arguments lastObject] mutableCopy];
        NSString* text = options[@"text"];
        if(text){
            KakaoTalkLinkObject *textObj
            = [KakaoTalkLinkObject createLabel:text];
            [kakaoArray addObject:textObj];
        }
        NSString* image = options[@"image"];
        if(image){
            NSDictionary *imageDic = [image copy];
            NSString* imageSrc = imageDic[@"src"];
            if(imageSrc){
                int width = 80;
                int height = 80;
                if(imageDic[@"width"] && [imageDic[@"width"] integerValue] >80){
                    width = [imageDic[@"width"] integerValue];
                }
                if(imageDic[@"height"] && [imageDic[@"height"] integerValue] >80){
                    height = [imageDic[@"height"] integerValue];
                }
                KakaoTalkLinkObject *imageObj
                = [KakaoTalkLinkObject createImage:imageSrc
                                        width:width
                                        height:height];
                [kakaoArray addObject:imageObj];
            }
        }
        NSString* weblink = options[@"weblink"];
        if(weblink){
            NSDictionary *weblinkDic = [weblink copy];
            NSString* weblinkUrl = weblinkDic[@"url"];
            NSString* weblinkText = weblinkDic[@"text"];
            if(weblinkUrl && weblinkText){
                KakaoTalkLinkObject *webLinkok
                = [KakaoTalkLinkObject createWebLink:weblinkText
                                       url:weblinkUrl];
                [kakaoArray addObject:webLinkok];
            }
        }
        NSString* applink = options[@"applink"];
        if(applink){
            NSDictionary *applinkDic = [applink copy];
            NSString* applinkUrl = applinkDic[@"url"];
            NSString* applinkText = applinkDic[@"text"];
            if(applinkUrl && applinkText){
                NSDictionary *params = nil;
                if(options[@"params"]){
                    params = options[@"params"];
                };
                KakaoTalkLinkAction *androidAppAction
                = [KakaoTalkLinkAction createAppAction:KakaoTalkLinkActionOSPlatformAndroid
                                                                        devicetype:KakaoTalkLinkActionDeviceTypePhone
                                                                         execparam:params];
                KakaoTalkLinkAction *iphoneAppAction
                = [KakaoTalkLinkAction createAppAction:KakaoTalkLinkActionOSPlatformIOS
                                                                        devicetype:KakaoTalkLinkActionDeviceTypePhone
                                                                         execparam:params];
                        
                        
                KakaoTalkLinkAction *webLinkActionUsingPC
                = [KakaoTalkLinkAction createWebAction:applinkUrl];

                KakaoTalkLinkObject *button
                = [KakaoTalkLinkObject createAppButton:applinkText
                                                                             actions:@[androidAppAction, iphoneAppAction, webLinkActionUsingPC]];
                [kakaoArray addObject:button];
            }
        }
        if(sizeof kakaoArray > 0){
            [KOAppCall openKakaoTalkAppLink:kakaoArray];
            CDVPluginResult* pluginResult = pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }else{
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid arguments"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
}

@end

