import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.io.FileInputStream;


public class CreateInvTxnsViaFBDI
{
    /*Parameters that need to be supplied by for each run*/
    public String ENV_URL= "https://abc-fa-ext.us.oracle.com";
    public String USER_NAME = "*************";
    public String PASS_WORD = "********";
    


    /*Constants per class - These can be changed depending on the type of upload. */
    public  String DOCUMENT_ACCOUNT = "scm$/inventoryTransaction$/import$";
    public  String JOB_NAME = "/oracle/apps/ess/scm/inventory/materialTransactions/txnManager,SingleTMEssJob";
    public String loadFileName = "<File Path>/InvTransactionsInterface.zip";
    //public String uploadFileName = "<NameOfTheFileInUCM>.zip";



    /*Constants - These shouldn't be changed. */
    public static final String IMPORT_OPERATION_NAME = "importBulkData";
    public static final String DOC_CONTENT_TYPE = "zip";
    public static final String OPER_POST = "POST";
    public static final String CONTENT_TYPE = "application/vnd.oracle.adf.resourceitem+json";
    public String INTERFACE_DETAILS_KEY = "33";

    public static boolean DEBUG_ON=true;

/*Points to consumers: 
1. Always use ZIP. Don't use Raw csv file.
2. Always use UTF-8 encoded.*/

    public static void main(String args[])
    {
        
        CreateInvTxnsViaFBDI client=new CreateInvTxnsViaFBDI();
        client.uploadFileName=args[0]+".zip";
        
        client.postFBDI();

    }
    /* 
    This is the core method that loads the FBDI data. The following are the steps executed by this method: 
    1. Get the Base64 representation of the content of the FBDI ZIP file. The name along with the location of the  FBDI ZIP 
       file is catpured by the class variable "loadFileName".
    2. Form the required payload to call the REST service by calling the method "formPayload()". 
    3. Invoke the REST API. 
    */
    public void postFBDI()
    {
        String docConent=getBase64FileContent(loadFileName); //Step 1

        String paylaod = formPayload(docConent,uploadFileName); // Step 2
        //Step 3 starts.
        URL url=null;
        HttpURLConnection conn = null;
        try {
            url = new URL(ENV_URL+"/fscmRestApi/resources/latest/erpintegrations/");

            conn = (HttpURLConnection)url.openConnection();

            conn.setRequestMethod(OPER_POST);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE);
            conn.setRequestProperty("Authorization", "Basic "+getAuthString(USER_NAME, PASS_WORD));
            conn.setDoOutput(true);
    
            OutputStream os = conn.getOutputStream();
            os.write(paylaod.getBytes());
            os.flush();
                InputStream is = null;
               logMsg("Response Code:"+conn.getResponseCode());
               if ((conn.getResponseCode()==201)) {
                   System.out.println("Request successful!!");
               }
               if (conn.getResponseCode() == 400) {
                is=conn.getErrorStream();
            }
            else {
                is=conn.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while( (line=br.readLine())!=null){                
            sb.append(line);
            }
            logMsg(sb.toString());


        } catch (MalformedURLException e) {
            e.printStackTrace();
            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getAuthString(String userName, String passWord)
    {
        try {
            String encoding = Base64.getEncoder().encodeToString((userName + ":" + passWord).getBytes("UTF-8"));
            logMsg(encoding);
            return encoding;

        } catch (Exception e) {
            e.printStackTrace();
            logMsg("Exception in getAuthString().."+e.getMessage());
            return null;
        }
    }
    public String formPayload(String documentContent, String fileName)
    {
        StringBuffer payload=new StringBuffer();
        payload.append("{");//Opening brace;
        payload.append("\"OperationName\":\"" + IMPORT_OPERATION_NAME+"\",");
        payload.append("\"DocumentContent\":\"" + documentContent+"\",");
        payload.append("\"ContentType\":\"" + DOC_CONTENT_TYPE+"\",");
        payload.append("\"FileName\":\"" + fileName+"\",");
        payload.append("\"DocumentAccount\":\"" + this.DOCUMENT_ACCOUNT+"\",");
        payload.append("\"JobName\":\"" + this.JOB_NAME+"\",");
        //payload.append("\"ParameterList\":"+"\"#NULL,Vision Operations,#NULL,#NULL,#NULL,#NULL,#NULL,INVOICE GATEWAY,#NULL,#NULL,#NULL,1,#NULL\",");
        payload.append("\"ParameterList\":"+"\"#NULL\",");
        payload.append("\"CallbackURL\":"+"\"#NULL\",");
        payload.append("\"NotificationCode\":"+"\"10\",");
        payload.append("\"JobOptions\":"+"\"InterfaceDetails="+INTERFACE_DETAILS_KEY+",ImportOption=N,PurgeOption = N,ExtractFileType=NONE\"");
        payload.append("}");//Closing brace;
        logMsg(payload.toString());
        return payload.toString();
    }
    public static void logMsg(String msg)
    {
        if ((DEBUG_ON)) {
            System.out.println(msg);
        }
    }
    public static String getBase64FileContent(String fileName)
    {
               StringBuffer dataBuffer=new StringBuffer();
        try {
            logMsg("Inside getBase64FileContent()");
        File file = new File(fileName);
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) 
        {// File is too large
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length &&(numRead = is.read(bytes, offset, bytes.length -offset)) >=0) 
        {
            offset += numRead;
        }
        if (offset < bytes.length) 
        {
            throw new IOException("Could not completely read file " +file.getName());
        }
        is.close();
        System.out.println("Bytes:"+bytes+"");
        byte[] encoded = Base64.getEncoder().encode(bytes);
        String encStr = new String(encoded);
        System.out.println("encStr:"+encStr);
        return encStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    return dataBuffer.toString();
    }
    
}

