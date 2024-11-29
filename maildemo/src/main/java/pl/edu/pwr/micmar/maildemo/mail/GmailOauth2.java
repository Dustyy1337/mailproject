package pl.edu.pwr.micmar.maildemo.mail;
import javax.mail.Message;
import javax.mail.Session;
import java.io.BufferedReader;
import java.io.InputStreamReader;
public class GmailOauth2 {
    public static String accessToken = "";
    public static String refreshToken = "";
    public static long expiration = 0;
    public static void refreshToken(SingleSession session) {
        ProcessBuilder pb = new ProcessBuilder(
                "python3", "oauth2.py",
                "--user="+session.username,
                "--client_id=285985730433-9v3aq615noh748trg4fq68opiqljugqu.apps.googleusercontent.com",
                "--client_secret=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",
                "--refresh_token="+session.refreshToken
        );
    }
    public static void getAccessToken(String email) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", "oauth2.py",
                    "--generate_oauth2_token",
                    "--quiet",
                    "--client_id=285985730433-9v3aq615noh748trg4fq68opiqljugqu.apps.googleusercontent.com",
                    "--client_secret=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",
                    "--user="+email
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Access Token:")) {
                    i++;
                    accessToken = line.split(": ")[1].trim();
                    //Message[] messages = EmailReader.fetchMessages(imapSession.getStore("imap"));
                    //for(int i = 0; i<10; i++) System.out.println(messages[i].getSubject());
                }
                if(line.startsWith("Refresh Token:")) {
                    i++;
                    refreshToken = line.split(": ")[1].trim();
                }
                if(line.startsWith("Access Token Expiration Seconds:")) {
                    i++;
                    long time = Long.parseLong(line.split(": ")[1].trim());
                    expiration = System.currentTimeMillis() + (time * 1000);
                }
                if(i == 3) {
                    EmailReader.connectToMail("imap.gmail.com", "mchlmarczak@gmail.com", GmailOauth2.accessToken, GmailOauth2.refreshToken, GmailOauth2.expiration);
                    process.destroy();
                    System.out.println("Process destroyed");
                    break;
                }
            }
            process.waitFor();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
