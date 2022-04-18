package cn.ztmzz.lychee_upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UploadUtil {

    private static String domain = "";
    private static final CookieStore cookieStore = new BasicCookieStore();
    private static String XSRF_TOKEN;

    public static void main(String[] args) throws Exception {
    }

    public static void set_domain(String domain) {
        UploadUtil.domain = domain;
    }

    public static String commonMethodEx(HttpEntity entity, String method) throws Exception {
        String url = domain + "/api/" + method;
        Executor executor = Executor.newInstance();
        BasicHeader xsrf = new BasicHeader("x-xsrf-token", XSRF_TOKEN);
        BasicHeader xml = new BasicHeader("x-requested-with", "XMLHttpRequest");
        String res = executor.use(cookieStore).execute(Request.Post(url).body(entity).addHeader(xsrf).addHeader(xml)).returnContent().asString();
        return res;
    }

    public static String commonMethod(String params, String method) throws Exception {
        StringEntity stringEntity = new StringEntity(params, "UTF-8");
        stringEntity.setContentType(String.valueOf(ContentType.APPLICATION_JSON));
        return commonMethodEx(stringEntity, method);
    }

    public static void init() throws Exception {
        String res = commonMethod("", "Session::init");
        List<Cookie> list = cookieStore.getCookies();
        for (Cookie c : list) {
            if (c.getName().equals("XSRF-TOKEN")) {
                XSRF_TOKEN = c.getValue();
                XSRF_TOKEN = XSRF_TOKEN.substring(0, XSRF_TOKEN.length() - 3);
                break;
            }
        }
//        System.out.println(res);
    }

    public static String albums_get() throws Exception {
        String res = commonMethod("", "Albums::get");
        return res;
    }

    public static String get_album_id(String album_name) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String res = albums_get();
        JsonNode json = mapper.readTree(res);
        json = json.get("albums");
        if (json.isArray()) {
            for (JsonNode node : json)
                if (node.get("title").asText().equals(album_name))
                    return node.get("id").asText();
        }
        return "false";
    }

    public static boolean album_get(String id) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("albumID", id);
        String res = commonMethod(json.toString(), "Album::get");
        return !res.equals("false");
    }

    public static String albums_add(String title, String parent_id) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("title", title);
        json.put("parent_id", parent_id);
        String res = commonMethod(json.toString(), "Album::add");
        return res;
    }

    public static String photo_get(String phoneID) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("photoID", phoneID);
        String res = commonMethod(json.toString(), "Photo::get");
        // System.out.println(res);
        return res;
    }

    public static String login(String username, String password) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("username", username);
        json.put("password", password);
        String res = commonMethod(json.toString(), "Session::login");
//        System.out.println(res);
        return res;
    }

    public static String photo_add(String album_name, String filepath) throws Exception {
        String albumID = get_album_id(album_name);
        if (albumID.equals("false")) {
            albumID = albums_add(album_name, "0");
        }
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("albumID", albumID);
        builder.addBinaryBody("0", new File(filepath), ContentType.APPLICATION_OCTET_STREAM, filepath);
        HttpEntity multipart = builder.build();
        String res = commonMethodEx(multipart, "Photo::add");
//        System.out.println(res);
        return res;
    }

    public static String upload(String filepath, String album_name) throws Exception {
        String photo_id = photo_add(album_name, filepath);
        System.out.println(photo_id);
        String photo_url_json = photo_get(photo_id);
        System.out.println(photo_url_json);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(photo_url_json);
        String photo_url = domain + '/' + node.get("url").asText();
        return photo_url;
    }
}
