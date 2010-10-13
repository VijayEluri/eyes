package models.probe;

import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import models.Server;
import play.Logger;
import play.db.jpa.Model;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

@Entity
public class HTTPProbe extends Model implements Probe {

  public static final String TYPE = "HTTP";

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public String name;
  public String serverURL;
  public Integer expectResponse;

  @Lob
  public ProbeResult status;

  public HTTPProbe(Server server, String name, String URL, int expectResponse) {
    this.server = server;
    this.name = name;
    this.serverURL = URL;
    this.expectResponse = expectResponse;
  }

  public String type() {
    return TYPE;
  }

  public String name() {
    return name;
  }

  public Server server() {
    return server;
  }

  public ProbeResult status() {
    return status;
  }

  public ProbeResult check() {
    ProbeResult result;

    WSRequest request = WS.url(serverURL);
    HttpResponse response = request.get();
    if (response.getStatus().equals(expectResponse)) {
      result = new ProbeResult(true);
    } else {
      String message = String.format("Server response %d but expect %d",
          response.getStatus(), expectResponse);
      Logger.error(message);
      result = new ProbeResult(false, message);
    }

    return result;
  }

}
