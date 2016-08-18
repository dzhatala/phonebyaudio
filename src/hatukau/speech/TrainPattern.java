package hatukau.speech;

import java.util.ArrayList;
import java.util.List;

public class TrainPattern {
	public static List <String> getAllTrainPatternName(){
		ArrayList <String >ret =new ArrayList<String>();
		
		ret.add("ba ba! ba!! ...   ba!! ba! ba");
		ret.add("ti ti! ti!! ...  ti!! ti! ti");
		ret.add("pe pe! pe!! ...  pe!! pe! pe");
		ret.add("dug dug! dug!! ...  dug!! dug! dug");
		ret.add("goc goc! goc!! ... goc!! goc! goc");
		ret.add("caf cif  cuf cef cof");
		ret.add("fig fug feg fog fag");
		ret.add("guh geh goh gah gih");
		ret.add("hek hok hak hik huk");
		ret.add("kol kal kil kul kel");
		ret.add("lam lim lum lem lom");
		ret.add("mis mus mes mos mas");
		ret.add("nur ner nor nar nir");
		ret.add("ren ron ran rin run");
		ret.add("sod sad sid sud sed");
		ret.add("wat wit wut wet wot");
		ret.add("yib yub yeb yob yab");
		ret.add("zung zeng zong zang zing");
		ret.add("ngez ngoz ngaz ngiz nguz");
		ret.add("nyob nyab nyib nyub nyeb");
		//diftong trainer
		ret.add("ai au ae ao ia iu ie io ua ui"); 
		ret.add("ue uo ea ei eu eo oa oi ou oe");
		ret.add("nyonya yang nonton film, makan sayuran bergizi,  pepaya hijau dan cabe rawit");
		
		
		return ret;
	}

}
