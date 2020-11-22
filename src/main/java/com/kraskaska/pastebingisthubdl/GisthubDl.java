package com.kraskaska.pastebingisthubdl;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GisthubDl {
    public static void main(String[] args) throws Exception{
        Set<String> runMimes = Set.of("application/x-python:python %s");
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url("https://api.github.com/gists/public")
                .get()
                .build();
        /*System.out.println("==PUBLIC GISTS==");
        System.out.println(client.newCall(req).execute().body().string());
        System.out.println("================");*/
        //Arrays.stream(args).forEach(System.out::println);
        var cmd = new Cmd(args[0], args[1], args[2], runMimes, null, client);
    }
    public static class Cmd {
        public Cmd(String mode, String gist_id, String gist_file, Set<String> mimes, String[] additionalOptions, OkHttpClient client) throws IOException {
            switch (mode) {
                case ("download"):{
                    System.out.println(String.format("[gisthub-dl] Downloading gist file %s/%s", gist_id, gist_file));
                    BufferedWriter bf = new BufferedWriter(new FileWriter(gist_file));
                    Request req = new Request.Builder()
                            .url("https://api.github.com/gists/"+gist_id)
                            .get()
                            .build();
                    String res = client.newCall(req).execute().body().string();
                    JSONObject obj = new JSONObject(res);
                    bf.write(obj.getJSONObject("files").getJSONObject(gist_file).getString("content"));
                    bf.flush();
                    bf.close();
                    System.out.println(String.format("[gisthub-dl] File saved as %s", gist_file));
                    break;
                }
                case ("run"):{
                    System.out.println(String.format("[gisthub-dl] Downloading gist file %s/%s", gist_id, gist_file));
                    BufferedWriter bf = new BufferedWriter(new FileWriter(gist_file));
                    Request req = new Request.Builder()
                            .url("https://api.github.com/gists/"+gist_id)
                            .get()
                            .build();
                    String res = client.newCall(req).execute().body().string();
                    JSONObject obj = new JSONObject(res);
                    bf.write(obj.getJSONObject("files").getJSONObject(gist_file).getString("content"));
                    bf.flush();
                    bf.close();
                    String mime = obj.getJSONObject("files").getJSONObject(gist_file).getString("type");
                    System.out.println("[gisthub-dl] Mime type: "+mime);
                    System.out.println(String.format("[gisthub-dl] File saved as %s", gist_file));
                    //HANDLING FILE
                    System.out.println("[gisthub-dl] Preparing to run file...");
                    AtomicBoolean runPerformed = new AtomicBoolean(false);
                    AtomicReference<String> cmd = new AtomicReference<>("");
                    mimes.forEach((x) -> {
                        var y = x.split(":");
                        if(y[0].trim().equalsIgnoreCase(mime.trim())) {
                            System.out.println(String.format("[gisthub-dl] Running %s with command \"%s\"", mime, String.format(y[1], gist_file)));
                            runPerformed.set(true);
                            cmd.set(String.format(y[1], gist_file));
                        }
                    });
                    if(!runPerformed.get()){
                        System.out.println("[gisthub-dl] Mime type for run not found! Maybe it is not executable file (data, json, etc) or it's bug!");
                    } else {
                        System.out.println("[gisthub-dl] started process");
                        Process process = new ProcessBuilder()
                                .command("cmd.exe", "/c", cmd.get())
                                .directory(new File("./"))
                                .inheritIO()
                                .start();
                    }
                    break;
                }
            }
        }
    }
}
