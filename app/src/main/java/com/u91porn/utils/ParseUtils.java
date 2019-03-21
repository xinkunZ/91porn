package com.u91porn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.u91porn.data.model.BaseResult;
import com.u91porn.data.model.UnLimit91PornItem;

/**
 * @author flymegoc
 * @date 2017/11/15
 * @describe
 */

public class ParseUtils {

  /**
   * 解析主页
   *
   * @param html
   *          主页html
   * @return 视频列表
   */
  public static List<UnLimit91PornItem> parseIndex(String html) {
    List<UnLimit91PornItem> unLimit91PornItemList = new ArrayList<>();
    Document doc = Jsoup.parse(html);
    Element body = doc.getElementById("tab-featured");
    Elements itms = body.select("p");
    for (Element element : itms) {
      UnLimit91PornItem unLimit91PornItem = new UnLimit91PornItem();

      String title = element.getElementsByClass("title").first().text();
      unLimit91PornItem.setTitle(title);
      Logger.d(title);

      String imgUrl = element.select("img").first().attr("src");
      unLimit91PornItem.setImgUrl(imgUrl);
      Logger.d(imgUrl);

      String duration = element.getElementsByClass("duration").first().text();
      unLimit91PornItem.setDuration(duration);
      Logger.d(duration);

      String contentUrl = element.select("a").first().attr("href");
      String viewKey = contentUrl.substring(contentUrl.indexOf("=") + 1);
      unLimit91PornItem.setViewKey(viewKey);
      Logger.d(viewKey);

      String allInfo = element.text();
      int start = allInfo.indexOf("添加时间");
      String info = allInfo.substring(start);

      unLimit91PornItem.setInfo(info);
      Logger.d(info);
      unLimit91PornItemList.add(unLimit91PornItem);
    }
    return unLimit91PornItemList;
  }

  /**
   * 解析其他类别
   *
   * @param html
   *          类别
   * @return 列表
   */
  public static BaseResult parseHot(String html) {
    int totalPage = 1;
    List<UnLimit91PornItem> unLimit91PornItemList = new ArrayList<>();
    Document doc = Jsoup.parse(html);
    Element body = doc.getElementById("fullside");

    Elements listchannel = body.getElementsByClass("listchannel");
    for (Element element : listchannel) {
      UnLimit91PornItem unLimit91PornItem = new UnLimit91PornItem();
      String contentUrl = element.select("a").first().attr("href");
      Logger.d(contentUrl);
      contentUrl = contentUrl.substring(0, contentUrl.indexOf("&"));
      Logger.d(contentUrl);
      String viewKey = contentUrl.substring(contentUrl.indexOf("=") + 1);
      unLimit91PornItem.setViewKey(viewKey);

      String imgUrl = element.select("a").first().select("img").first().attr("src");
      Logger.d(imgUrl);
      unLimit91PornItem.setImgUrl(imgUrl);

      String title = element.select("a").first().select("img").first().attr("title");
      Logger.d(title);
      unLimit91PornItem.setTitle(title);

      String allInfo = element.text();

      int sindex = allInfo.indexOf("时长");

      String duration = allInfo.substring(sindex + 3, sindex + 8);
      unLimit91PornItem.setDuration(duration);

      int start = allInfo.indexOf("添加时间");
      String info = allInfo.substring(start);
      unLimit91PornItem.setInfo(info.replace("还未被评分", ""));
      Logger.d(info);

      unLimit91PornItemList.add(unLimit91PornItem);
    }
    // 总页数
    Element pagingnav = body.getElementById("paging");
    Elements a = pagingnav.select("a");
    if (a.size() > 2) {
      String ppp = a.get(a.size() - 2).text();
      if (TextUtils.isDigitsOnly(ppp)) {
        totalPage = Integer.parseInt(ppp);
        Logger.d("总页数：" + totalPage);
      }
    }
    BaseResult baseResult = new BaseResult();
    baseResult.setTotalPage(totalPage);
    baseResult.setUnLimit91PornItemList(unLimit91PornItemList);
    return baseResult;
  }

  /**
   * 解析视频播放连接
   *
   * @param html
   *          视频页
   * @return 视频连接
   */
  public static String parseVideoPlayUrl(String html) throws Exception {
    String videoUrl = null;
    try {
      if (html.contains("你每天只可观看10个视频")) {
        Logger.d("已经超出观看上限了");
        return "";
      }
      Document doc = Jsoup.parse(html);
      videoUrl = doc.select("video").first().select("source").first().attr("src");
      Logger.d("视频链接：" + videoUrl);
      String thumImg = doc.getElementById("vid").attr("poster");
      Logger.d("缩略图：" + thumImg);
    } catch (Exception e) {
      Logger.e("解析出错：", e);
    }
    if (videoUrl == null) {
      videoUrl = parseVideoPlayUrl190321(html);
    }
    if (videoUrl == null) {
      throw new Exception("解析失败！！");
    } else {
      return videoUrl;
    }
  }

  /**
   * 新版91porn使用了加密，将url放在了注释里，此方法用于尝试解析
   * 
   * @param html
   * @return
   * @throws Exception
   */
  private static String parseVideoPlayUrl190321(String html) throws Exception {
    Document doc = Jsoup.parse(html);
    Element video = doc.select("video").first();
    Pattern p = Pattern.compile(".*document.write\\(strencode\\(\"(.*)\",\"(.*)\",\"(.*)\"");
    Matcher m = p.matcher(video.toString());
    if (m.find()) {
      String param1 = m.group(1);
      String param2 = m.group(2);
      String param3 = m.group(3);
      String source = decode(param1, param2, param3);
      return Jsoup.parse(source).select("source").attr("src");
    }
    return null;
  }

  private static String decode(String param1, String param2, String param3) throws Exception {
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
    engine
        .eval(";var encode_version = 'sojson.v5', lbbpm = '__0x33ad7',  __0x33ad7=['QMOTw6XDtVE=','w5XDgsORw5LCuQ==','wojDrWTChFU=','dkdJACw=','w6zDpXDDvsKVwqA=','ZifCsh85fsKaXsOOWg==','RcOvw47DghzDuA==','w7siYTLCnw=='];(function(_0x94dee0,_0x4a3b74){var _0x588ae7=function(_0x32b32e){while(--_0x32b32e){_0x94dee0['push'](_0x94dee0['shift']());}};_0x588ae7(++_0x4a3b74);}(__0x33ad7,0x8f));var _0x5b60=function(_0x4d4456,_0x5a24e3){_0x4d4456=_0x4d4456-0x0;var _0xa82079=__0x33ad7[_0x4d4456];if(_0x5b60['initialized']===undefined){(function(){var _0xef6e0=typeof window!=='undefined'?window:typeof process==='object'&&typeof require==='function'&&typeof global==='object'?global:this;var _0x221728='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';_0xef6e0['atob']||(_0xef6e0['atob']=function(_0x4bb81e){var _0x1c1b59=String(_0x4bb81e)['replace'](/=+$/,'');for(var _0x5e3437=0x0,_0x2da204,_0x1f23f4,_0x3f19c1=0x0,_0x3fb8a7='';_0x1f23f4=_0x1c1b59['charAt'](_0x3f19c1++);~_0x1f23f4&&(_0x2da204=_0x5e3437%0x4?_0x2da204*0x40+_0x1f23f4:_0x1f23f4,_0x5e3437++%0x4)?_0x3fb8a7+=String['fromCharCode'](0xff&_0x2da204>>(-0x2*_0x5e3437&0x6)):0x0){_0x1f23f4=_0x221728['indexOf'](_0x1f23f4);}return _0x3fb8a7;});}());var _0x43712e=function(_0x2e9442,_0x305a3a){var _0x3702d8=[],_0x234ad1=0x0,_0xd45a92,_0x5a1bee='',_0x4a894e='';_0x2e9442=atob(_0x2e9442);for(var _0x67ab0e=0x0,_0x1753b1=_0x2e9442['length'];_0x67ab0e<_0x1753b1;_0x67ab0e++){_0x4a894e+='%'+('00'+_0x2e9442['charCodeAt'](_0x67ab0e)['toString'](0x10))['slice'](-0x2);}_0x2e9442=decodeURIComponent(_0x4a894e);for(var _0x246dd5=0x0;_0x246dd5<0x100;_0x246dd5++){_0x3702d8[_0x246dd5]=_0x246dd5;}for(_0x246dd5=0x0;_0x246dd5<0x100;_0x246dd5++){_0x234ad1=(_0x234ad1+_0x3702d8[_0x246dd5]+_0x305a3a['charCodeAt'](_0x246dd5%_0x305a3a['length']))%0x100;_0xd45a92=_0x3702d8[_0x246dd5];_0x3702d8[_0x246dd5]=_0x3702d8[_0x234ad1];_0x3702d8[_0x234ad1]=_0xd45a92;}_0x246dd5=0x0;_0x234ad1=0x0;for(var _0x39e824=0x0;_0x39e824<_0x2e9442['length'];_0x39e824++){_0x246dd5=(_0x246dd5+0x1)%0x100;_0x234ad1=(_0x234ad1+_0x3702d8[_0x246dd5])%0x100;_0xd45a92=_0x3702d8[_0x246dd5];_0x3702d8[_0x246dd5]=_0x3702d8[_0x234ad1];_0x3702d8[_0x234ad1]=_0xd45a92;_0x5a1bee+=String['fromCharCode'](_0x2e9442['charCodeAt'](_0x39e824)^_0x3702d8[(_0x3702d8[_0x246dd5]+_0x3702d8[_0x234ad1])%0x100]);}return _0x5a1bee;};_0x5b60['rc4']=_0x43712e;_0x5b60['data']={};_0x5b60['initialized']=!![];}var _0x4be5de=_0x5b60['data'][_0x4d4456];if(_0x4be5de===undefined){if(_0x5b60['once']===undefined){_0x5b60['once']=!![];}_0xa82079=_0x5b60['rc4'](_0xa82079,_0x5a24e3);_0x5b60['data'][_0x4d4456]=_0xa82079;}else{_0xa82079=_0x4be5de;}return _0xa82079;};if(typeof encode_version!=='undefined'&&encode_version==='sojson.v5'){function strencode(_0x50cb35,_0x1e821d){var _0x59f053={'MDWYS':'0|4|1|3|2','uyGXL':function _0x3726b1(_0x2b01e8,_0x53b357){return _0x2b01e8(_0x53b357);},'otDTt':function _0x4f6396(_0x33a2eb,_0x5aa7c9){return _0x33a2eb<_0x5aa7c9;},'tPPtN':function _0x3a63ea(_0x1546a9,_0x3fa992){return _0x1546a9%_0x3fa992;}};var _0xd6483c=_0x59f053[_0x5b60('0x0','cEiQ')][_0x5b60('0x1','&]Gi')]('|'),_0x1a3127=0x0;while(!![]){switch(_0xd6483c[_0x1a3127++]){case'0':_0x50cb35=_0x59f053[_0x5b60('0x2','ofbL')](atob,_0x50cb35);continue;case'1':code='';continue;case'2':return _0x59f053[_0x5b60('0x3','mLzQ')](atob,code);case'3':for(i=0x0;_0x59f053[_0x5b60('0x4','J2rX')](i,_0x50cb35[_0x5b60('0x5','Z(CX')]);i++){k=_0x59f053['tPPtN'](i,len);code+=String['fromCharCode'](_0x50cb35[_0x5b60('0x6','s4(u')](i)^_0x1e821d['charCodeAt'](k));}continue;case'4':len=_0x1e821d[_0x5b60('0x7','!Mys')];continue;}break;}}}else{alert('');};");
    if (engine instanceof Invocable) {
      Invocable invocable = (Invocable) engine;
      Encode executeMethod = invocable.getInterface(Encode.class);
      return executeMethod.strencode(param1, param2, param3);
    } else {
      throw new Exception("解码失败！");
    }
  }

  interface Encode {
    String strencode(String param1, String param2, String param3);
  }
}
