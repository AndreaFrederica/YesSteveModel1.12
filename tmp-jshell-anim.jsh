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
    var main = m.mainEntity.animationFiles.get("main");
    System.out.println("main animations count=" + (main == null ? -1 : main.animations.size()));
    if (main != null) {
      int i = 0;
      for (var e : main.animations.entrySet()) {
        System.out.println("  anim=" + e.getKey());
        if (++i >= 40) break;
      }
    }
  }
}
/exit
