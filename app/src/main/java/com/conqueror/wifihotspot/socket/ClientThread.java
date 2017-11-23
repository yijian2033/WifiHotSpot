package com.conqueror.wifihotspot.socket;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author yijian2033
 * @date on 2017/11/22
 * @describe TODO
 */

public class ClientThread implements Runnable {

    private String json;
    private Context context;
    private Handler handler;
    private StringBuilder result;

    public ClientThread(Context context, Handler handler, String json) {

        this.context = context;
        this.handler = handler;
        this.json = json;
    }


    @Override
    public void run() {

        try {
            //1.创建客户端Socket，指定服务器地址和端口
            Socket socket = new Socket("192.168.100.99", 8088);
            socket.setSoTimeout(1000);
            Log.i("host", "---------------------------");
            //2.获取输出流，向服务器端发送信息
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
            pw.write(json);
            pw.flush();
            socket.shutdownOutput();//关闭输出流

            //3.获取输入流，并读取服务器端的响应信息
            InputStream is = socket.getInputStream();
            result = new StringBuilder();
            byte[] bus = new byte[1024];
            int length = 0;
            while ((length = is.read(bus)) != 0) {
                result.append(new String(bus, 0, length));
            }
            Log.i("host", "get device result : " + result.toString());
            Message msg = new Message();
            Bundle bundle = new Bundle();
            //如果没有数据那就是没有连接到服务器
            if (result.length() == 0) {
                msg.what = 0;
            } else {
                //将数据发送给主线程
                bundle.putString("json", result.toString());
                msg.what = 8088;
                msg.setData(bundle);
            }

            handler.sendMessage(msg);

//            InputStream is = socket.getInputStream();
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            byte[] bytes = new byte[1024];
//            String info = null;
//            while ((info = br.readLine()) != null) {
////                System.out.println("我是客户端，服务器说：" + info);
//                final String finalInfo = info;
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        Toast.makeText(context, "服务器返回的信息：" + finalInfo, Toast.LENGTH_SHORT)
// .show();
//                    }
//                });
//            }
            //4.关闭资源
//            br.close();
            is.close();
            pw.close();
            os.close();
            socket.close();
        } catch (UnknownHostException e) {
            Log.e("host", e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("host", e.toString());
            e.printStackTrace();
        } finally {
            if (result == null) {
                handler.sendEmptyMessage(0);
            }
        }
    }

}
