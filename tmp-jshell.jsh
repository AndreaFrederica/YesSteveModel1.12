import java.nio.file.*;
import rip.ysm.security.YsmCrypt;
import com.elfmcys.yesstevemodel.resource.YSMBinaryDeserializer;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;

for (String name : new String[]{"香草.ysm","草莓.ysm","江风.ysm","巧克力.ysm"}) {
  System.out.println("=== " + name + " ===");
  byte[] raw = Files.readAllBytes(Paths.get("D:/Projects/TheYSMFor1.12.2/ysm_test/" + name));
  byte[] dec = YsmCrypt.decryptYsmFile(raw);
  try (YSMBinaryDeserializer d = new YSMBinaryDeserializer(dec)) {
    RawYsmModel m = d.deserializeKeepOpen();
    d.parseYSMFooter(m);
    System.out.println("modelId=" + m.modelId + ", controllerFiles=" + m.mainEntity.animationControllerFiles.size() + ", animationFiles=" + m.mainEntity.animationFiles.keySet());
    for (var f : m.mainEntity.animationControllerFiles) {
      System.out.println("file=" + f.name + ", controllers=" + f.controllers.size());
      int i = 0;
      for (var e : f.controllers.entrySet()) {
        System.out.println("  key=" + e.getKey());
        if (++i >= 20) break;
      }
    }
  }
}
/exit
