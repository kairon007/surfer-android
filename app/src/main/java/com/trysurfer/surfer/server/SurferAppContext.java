package com.trysurfer.surfer.server;

/**
 * Created by PRO on 10/9/2014.
 */
/**
 * Single class to hold name and password, so
 * it could be used for rest calls
 */

// Not in use
public class SurferAppContext {

    private static SurferAppContext instance = null;
    private String id;
    private String password;
    private String baseUrl;

    //private HttpHeaders defaultHeaders = null;

    private SurferAppContext(){

    }

    public static SurferAppContext getInstance(){
        if(instance == null){
            instance = new SurferAppContext();
        }
        return instance;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getBaseUrl(){
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl){
        this.baseUrl = baseUrl;
    }
}
	/*
	public HttpHeaders getDefaultHeaders(){
		if(defaultHeaders == null){
			//HttpAuthentication authHeader = new HttpBasicAuthentication(id, password);
			defaultHeaders = new HttpHeaders();
			//defaultHeaders.add("Accept", "application/json");
			defaultHeaders.setContentType(MediaType.APPLICATION_JSON);
			defaultHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			//defaultHeaders.setAuthorization(authHeader);
			//requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		}
		return defaultHeaders;
	}
	*/

/*
	public RestTemplate getDefaultRestTemplate(){
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		/*There is a bug with Spring/HTTP Client and sometimes it causes
		 * EOFException when POSTing. But adding the following line, setting new HTTP
		 * client request factory solves that problem. But when SSL with self-signed
		 * will not work if this is set. So uncomment  the following line if SSL is not
		 * used. Since SSL is being used for ToDo resource, I had comment this out for now
		 * TODO: Research more and solves this or stop using Spring RestTemplate
		 */
/*
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		return restTemplate;
	}
}
*/
