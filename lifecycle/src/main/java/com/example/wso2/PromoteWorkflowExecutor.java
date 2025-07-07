package com.example.wso2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.APIStateChangeSimpleWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.APIStateWorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.axis2.util.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpHeaders;

public class PromoteWorkflowExecutor extends APIStateChangeSimpleWorkflowExecutor {
    
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(PromoteWorkflowExecutor.class);
	
    // Those values are initialized from the internal APIM properties/secrets
	private String githubToken;
    private String githubOwner;
    private String githubRepo;
    private String workflowFileName;

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {


        //Map<Integer, Integer> subscriberMap = new HashMap<>();
        APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;
        
        log.error("Executed State: "+ apiStateWorkFlowDTO.getApiCurrentState());
        
        String lcAction = apiStateWorkFlowDTO.getApiLCAction();
        log.error("LC Action: "+ lcAction);
       
        // we are in a transition from PUBLISHED->PROMOTED
        if (lcAction != null && "Promote".equalsIgnoreCase(lcAction.trim())) {
        	
            String githubApiUrl = String.format("https://api.github.com/repos/%s/%s/actions/workflows/%s/dispatches", 
                                               getGithubOwner(), getGithubRepo(), getWorkflowFileName());
            
            URL serviceEndpointURL = new URL(githubApiUrl);
            HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
            HttpPost httpPost = new HttpPost(githubApiUrl);
            
            // Set GitHub API headers
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getGithubToken());
            httpPost.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            
            // Create JSON payload for workflow dispatch
            String jsonPayload = String.format(
                "{\"ref\":\"main\",\"inputs\":{\"apiName\":\"%s\",\"apiVersion\":\"%s\"}}",
                apiStateWorkFlowDTO.getApiName(), apiStateWorkFlowDTO.getApiVersion());
            
            try {
				httpPost.setEntity(new StringEntity(jsonPayload));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				String errorMsg = "Error building HTTP request";
				log.error(errorMsg, e);
				throw new WorkflowException(errorMsg, e);
			}

            try {
                HttpResponse response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                String responseMsg = response.getStatusLine().getReasonPhrase();
                log.info("GitHub workflow triggered - Status: " + statusCode + " " + responseMsg);
                
                if (statusCode == 204) {
                    log.info("Successfully triggered GitHub workflow: " + getWorkflowFileName());
                } else {
                    log.error("Failed to trigger GitHub workflow during Promote. Status: " + statusCode);
                    throw new WorkflowException("Failed to start GH workflow");
                }
            
             } catch (ClientProtocolException e) {
                String errorMsg = "Error while calling GitHub API";
                log.error(errorMsg, e);
                throw new WorkflowException(errorMsg, e);
            } catch (IOException e) {
                String errorMsg = "Error triggering GitHub workflow";
                log.error(errorMsg, e);
                throw new WorkflowException(errorMsg, e);
            }
        }
        
        // Execute default workflow actions
        return super.execute(workflowDTO);

    }

	public String getWorkflowFileName() {
		return workflowFileName;
	}

	public void setWorkflowFileName(String workflowFileName) {
		this.workflowFileName = workflowFileName;
	}

	public String getGithubRepo() {
		return githubRepo;
	}

	public void setGithubRepo(String githubRepo) {
		this.githubRepo = githubRepo;
	}

	public String getGithubOwner() {
		return githubOwner;
	}

	public void setGithubOwner(String githubOwner) {
		this.githubOwner = githubOwner;
	}

	public String getGithubToken() {
		return githubToken;
	}

	public void setGithubToken(String githubToken) {
		this.githubToken = githubToken;
	}
    
}
