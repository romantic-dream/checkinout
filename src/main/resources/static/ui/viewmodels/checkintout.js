const ticket = 'HoagFKDcsGMVCIY2vOjf9h4fRuzsbE5TzcnPMTHxlj6olx3N4GckLUx4mtEQgO9YikYnmcmepzqhoLSpdut_qQ';

wx.config({
    debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
    appId: 'wx2938bf4d8010cb83', // 必填，公众号的唯一标识
    timestamp: 1548379300842, // 必填，生成签名的时间戳
    nonceStr: '1234567890abcdef', // 必填，生成签名的随机串
    signature: '075e9e2d0fc8e3bf21c318760570e233cbfefe3f',// 必填，签名
    jsApiList: [
        'checkJsApi',
        'chooseImage',
        'getLocation',
        'openLocation',
        'scanQRCode'
    ] // 必填，需要使用的JS接口列表
});

wx.ready(function () {
    // config信息验证后会执行ready方法，所有接口调用都必须在config接口获得结果之后，config是一个客户端的异步操作，所以如果需要在页面加载时就调用相关接口，则须把相关接口放在ready函数中调用来确保正确执行。对于用户触发时才调用的接口，则可以直接调用，不需要放在ready函数中。
    console.log('验证成功');
});

wx.error(function (error) {
    // config信息验证失败会执行error函数，如签名过期导致验证失败，具体错误信息可以打开config的debug模式查看，也可以在返回的res参数中查看，对于SPA可以在这里更新签名。
    console.log(error);
});

var app = new Vue({
    el: '#app',
    data: {

    },
    methods: {
        handleCheckInOut() {
            console.log('check inout click');
        },
        checkJsApi() {
            wx.checkJsApi({
                jsApiList: ['chooseImage'], // 需要检测的JS接口列表，所有JS接口列表见附录2,
                success: function (res) {
                    // 以键值对的形式返回，可用的api值true，不可用为false
                    // 如：{"checkResult":{"chooseImage":true},"errMsg":"checkJsApi:ok"}
                    console.log(res);
                }, fail: function (error) {
                    console.log(error);
                }
            });
        },
        chooseImage() {
            wx.chooseImage({
                count: 2, // 默认9
                sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
                sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
                success: function (res) {
                    console.log(res);
                }, fail: function (error) {
                    console.log(error);
                }
            });
        },
        openLocation() {
            wx.openLocation({
                latitude: 0, // 纬度，浮点数，范围为90 ~ -90
                longitude: 0, // 经度，浮点数，范围为180 ~ -180。
                name: '', // 位置名
                address: '', // 地址详情说明
                scale: 1, // 地图缩放级别,整形值,范围从1~28。默认为最大
                infoUrl: '', // 在查看位置界面底部显示的超链接,可点击跳转
                success: function (res) {
                    console.log(res);
                },
                fail: function (err) {
                    console.error(err);
                }
            });
        },
        getLocation() {
            wx.getLocation({
                type: 'wgs84', // 默认为wgs84的gps坐标，如果要返回直接给openLocation用的火星坐标，可传入'gcj02'
                success: function (res) {
                    wx.openLocation({
                        latitude: res.latitude, // 纬度，浮点数，范围为90 ~ -90
                        longitude: res.longitude, // 经度，浮点数，范围为180 ~ -180。
                        name: '', // 位置名
                        address: '', // 地址详情说明
                        scale: 28, // 地图缩放级别,整形值,范围从1~28。默认为最大
                        infoUrl: '', // 在查看位置界面底部显示的超链接,可点击跳转
                        success: function (res) {
                            console.log(res);
                        },
                        fail: function (err) {
                            console.error(err);
                        }
                    });
                }, fail: function (error) {
                    console.log(error);
                }
            });
        },
        scanQRCode() {
            wx.scanQRCode({
                needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
                scanType: ["qrCode", "barCode"], // 可以指定扫二维码还是一维码，默认二者都有
                success: function (res) {
                    var result = res.resultStr; // 当needResult 为 1 时，扫码返回的结果
                    console.log(result);
                },
                fail:function(error){
                    console.log(error);
                }
            });
        }
    }
});