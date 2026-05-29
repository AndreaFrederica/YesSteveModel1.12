import java.nio.file.*;
import rip.ysm.security.YsmCrypt;
import com.elfmcys.yesstevemodel.resource.YSMBinaryDeserializer;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;

for (String name : new String[]{"香草.ysm","草莓.ysm","江风.ysm","巧克力.ysm"}) {
  byte[] raw = Files.readAllBytes(Paths.get("D:/Projects/TheYSMFor1.12.2/ysm_test/" + name));
  byte[] dec = YsmCrypt.decryptYsmFile(raw);
  try (YSMBinaryDeserializer d = new YSMBinaryDeserializer(dec)) {
    RawYsmModel m = d.deserializeKeepOpen();
    var keys = m.mainEntity.animationFiles.get("main").animations.keySet();
    System.out.println(name + " walk=" + keys.contains("walk") + ", idle=" + keys.contains("idle") + ", fly=" + keys.contains("fly") + ", run=" + keys.contains("run"));
  }
}
/exit
