package com.sijin.free.main;

import com.sijin.free.po.DockInfo;
import com.sijin.free.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by sijinzhang on 16/8/24.
 */
public class Fe {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Fe.class);
    static HttpClientPoolUtill httpClient = new HttpClientPoolUtill(2,1);
    public static String url ="http://hq.sinajs.cn/list=";
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try {
            all();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("i have finished it,spends ----->" + (System.currentTimeMillis() - start) / 1000 + "s");
    }

    private static void all() throws Exception {

        List<DockInfo> list = FileUtil.loadDockFromConf("/Users/sijinzhang/git/datapp/free-info/src/main/resources/fe.properties");
        if(CollectionUtils.isEmpty(list)){return;}
        while (true){
            try {
                StringBuffer sb = new StringBuffer();
                Map<String,DockInfo> map = new HashMap<>();
                for(DockInfo dockInfo : list){
                    sb.append(dockInfo.getCode()).append(",");
                    map.put(dockInfo.getCode(),dockInfo);
                }
                String posturl = url+ sb.deleteCharAt(sb.length()-1).toString();
                String result = httpClient.get(posturl);
                String arrp[] = result.split(";\n");
                List<DockInfo> resultList = new ArrayList<>();
                for(String r : arrp){
                    DockInfo inf = new DockInfo();
                    inf = CovertUtil.covertToDockInfo(result);
                    inf.setMywantbuy(map.get(inf.getCode()).getMywantbuy());
                    inf.setMywantsale(map.get(inf.getCode()).getMywantsale());
                    resultList.add(inf);
                }
                handle(resultList);
                Thread.sleep(10000);
            }catch (Exception e){

            }

        }
    }

    private static void handle(List<DockInfo> dockInfolist) {
        String subject = "";
        List<String> messageList = new ArrayList<String>();
        for(DockInfo dockInfo : dockInfolist){
            if(dockInfo.getlPrice().compareTo(dockInfo.getMywantbuy()) <=0 &&
                    dockInfo.getPrice().compareTo(dockInfo.getMywantbuy()) <=0 ){
                subject = dockInfo.getName() + "[" + dockInfo.getCode() + "]" + " price is " + dockInfo.getPrice() + ",less than " + dockInfo.getMywantbuy() ;
                dockInfo.setMailMessage(subject);
                messageList.add(subject);
            }
            if(dockInfo.gethPrice().compareTo(dockInfo.getMywantsale()) >=0 &&
                    dockInfo.getPrice().compareTo(dockInfo.getMywantsale()) >=0 ){
                subject = dockInfo.getName() + "[" + dockInfo.getCode() + "]" + " price is " + dockInfo.getPrice() + ",more than " + dockInfo.getMywantsale() ;
                dockInfo.setMailMessage(subject);
                messageList.add(subject);
            }
        }

        Collections.sort(dockInfolist, new Comparator<DockInfo>() {
            public int compare(DockInfo o1, DockInfo o2) {
                if (o1.getPrice() - o2.getPrice() >= 0) {
                    return 1;
                }
                if (o1.getPrice() - o2.getPrice() < 0) {
                    return -1;
                }

                return 0;
            }
        });

        for(int i=0;i<dockInfolist.size();i++){
            System.out.println("["+ (i+1) +"]->"+ dockInfolist.get(i));
            if(dockInfolist.get(i).getMailMessage()!=null){
                MailUtil.sendMessage(dockInfolist.get(i).getMailMessage(), dockInfolist.get(i).toString());
            }
        }

        for (String s : messageList){
            System.out.println(s);
        }

        System.out.println("------------------------------------>" + DateUtils.formatDateTime(new Date()) + "<-------------------------------------");
    }
}


























