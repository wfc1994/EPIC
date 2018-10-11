package cn.edu.zjut.myong.epic.model;

import cn.edu.zjut.myong.epic.Game;
import cn.edu.zjut.myong.epic.GameAction;
import cn.edu.zjut.myong.epic.GamePattern;
import cn.edu.zjut.myong.epic.GamePlayer;

import javax.json.*;
import java.io.StringReader;
import java.util.*;

public class ICModel extends ICRedistributableModel {
    private static final long serialVersionUID = 2404188334581218504L;

    private Random rand = new Random(new Date().getTime());

    /*
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream
                ("./configuration.json")));
        String line = "";
        String json = "";
        while ((line = reader.readLine()) != null) {
            json = json + line;
        }
        reader.close();
        ICConfiguration icc = new ICConfiguration(json.trim());

        Experiment.init(icc);

        ICModel icmod = new ICModel();
        icmod.setParameters(Experiment.conf.getModelParameters());
        ICPattern ptn0 = (ICPattern) icmod.getInitial();
        //icmod.printPatten(ptn0);

        Random rand = new Random(new Date().getTime());
        ICAction actA = new ICAction();
        actA.action = new int[icmod.params.K][icmod.params.K];
        ICAction actB = new ICAction();
        actB.action = new int[icmod.params.K][icmod.params.K];
        for (Grid2DIndex idx : ptn0.patternA.keySet()) {
            if (ptn0.patternA.get(idx)[icmod.segNum-1] == ICPattern.EXIST && rand.nextDouble() < 0.5) {
                actA.action[idx.x][idx.y] = ICAction.EXTRACT;
            }
        }
        //actA.print();
        //System.out.println("---------------");
        for (Grid2DIndex idx : ptn0.patternB.keySet()) {
            if (ptn0.patternB.get(idx)[icmod.segNum-1] == ICPattern.EXIST && rand.nextDouble() < 0.5) {
                actB.action[idx.x][idx.y] = ICAction.EXTRACT;
            }
        }
        //actB.print();

        Game grp = Experiment.groups.get(1);
        Map<String, GameAction> actions = new HashMap<>();
        actions.put(grp.getRoleCodes(GamePlayer.ROLE.A).get(0), actA);
        actions.put(grp.getRoleCodes(GamePlayer.ROLE.B).get(0), actB);

        ICPattern ptn1 = (ICPattern) icmod.simulate(grp, ptn0, actions);
    }
    */

    public enum SIMSTEP {
        ini,
        hvt,
        mov,
        dyn,
    }

    /*
    private void printPatten(ICPattern ptn) {
        for (int s = 0; s < segNum; s++) {
            for (int i = 0; i < params.K; i++) {
                for (int j = 0; j < params.K; j++) {
                    Grid2DIndex idx = new Grid2DIndex(i,j);
                    if (ptn.patternA.containsKey(idx))
                        System.out.print(ptn.patternA.get(idx)[s] + " ");
                    else
                        System.out.print("0 ");
                }
                System.out.print("  |  ");
                for (int j = 0; j < params.K; j++) {
                    Grid2DIndex idx = new Grid2DIndex(i,j);
                    if (ptn.patternB.containsKey(idx))
                        System.out.print(ptn.patternB.get(idx)[s] + " ");
                    else
                        System.out.print("0 ");
                }
                System.out.println("");
            }
            System.out.println("---------------\n");
        }
    }
    */

    private ICParameters params = null;
    private ICPattern iniPtn = null;

    @Override
    public GamePattern getInitial() {
        if (iniPtn == null) {
            iniPtn = new ICPattern();
            List<Set<Grid2DIndex>> ip = randomDist(params.iniA, params.iniB);
            iniPtn.patternA.put(SIMSTEP.ini, new HashSet<Grid2DIndex>());
            iniPtn.patternA.put(SIMSTEP.hvt, new HashSet<Grid2DIndex>());
            iniPtn.patternA.put(SIMSTEP.mov, new HashSet<Grid2DIndex>());
            iniPtn.patternA.put(SIMSTEP.dyn, ip.get(0));
            iniPtn.patternB.put(SIMSTEP.ini, new HashSet<Grid2DIndex>());
            iniPtn.patternB.put(SIMSTEP.hvt, new HashSet<Grid2DIndex>());
            iniPtn.patternB.put(SIMSTEP.mov, new HashSet<Grid2DIndex>());
            iniPtn.patternB.put(SIMSTEP.dyn, ip.get(1));
        }
        return iniPtn;
    }

    @Override
    public JsonArrayBuilder getJsonInitial() {
        if (iniPtn == null)
            getInitial();
        return iniPtn.toJson();
    }

    @Override
    public GameAction parseAction(String json) {
        ICAction ppsAction = new ICAction();
        ppsAction.action = new int[params.K][params.K];

        JsonReader jsonReader = Json.createReader(new StringReader(json));
        JsonArray array = jsonReader.readArray();
        JsonArray act = array.getJsonArray(0);
        for (int i = 0; i < params.K; i++) {
            JsonArray acti = act.getJsonArray(i);
            for (int j = 0; j < params.K; j++) {
                ppsAction.action[i][j] = acti.getInt(j);
            }
        }
        ppsAction.redist = super.parseRedistributingAction(array.getJsonObject(1));

        return ppsAction;
    }

    @Override
    public GamePattern simulate(Game group, GamePattern state0, Map<String, GameAction> action) {
        //
        Harvest.dViewers.get(group.id).renew();

        // 前后两个patterns
        ICPattern prestate = (ICPattern) state0;
        ICPattern newstate = new ICPattern();

        System.out.println(action.keySet());

        // 两种资源分别的行为
        List<String> As = group.getRoleCodes(GamePlayer.ROLE.A);
        List<String> Bs = group.getRoleCodes(GamePlayer.ROLE.B);

        System.out.println("A: " + As);
        System.out.println("B: " + Bs);

        for (String c : action.keySet()) {
            System.out.println(c);
            ((ICAction) action.get(c)).print();
        }

        ICAction actA = new ICAction();
        actA.action = new int[params.K][params.K];
        ICAction actB = new ICAction();
        actB.action = new int[params.K][params.K];
        for (String code : action.keySet()) {
            GamePlayer.ROLE role = group.players.get(code).role;
            int w = 0;
            for (int i = 0; i < params.K; i++) {
                for (int j = 0; j < params.K; j++) {
                    ICAction ica = (ICAction) action.get(code);
                    if (ica.action[i][j] == ICAction.EXTRACT) {
                        if (role == GamePlayer.ROLE.A)
                            actA.action[i][j] = ICAction.EXTRACT;
                        else
                            actB.action[i][j] = ICAction.EXTRACT;
                        w++;
                    }
                }
            }
            ICRedistributableBenefit bene = new ICRedistributableBenefit();
            bene.income = w;
            newstate.benefit.put(code, bene);
        }

        System.out.println("actA");
        actA.print();
        System.out.println("actB");
        actB.print();

        // 新的pattern
        newstate.patternA.put(SIMSTEP.ini, prestate.patternA.get(SIMSTEP.dyn));
        newstate.patternB.put(SIMSTEP.ini, prestate.patternB.get(SIMSTEP.dyn));

        //
        System.out.println("Initialized");
        newstate.print();

        // 几个流程
        // 1. 收获
        newstate.patternA.put(SIMSTEP.hvt, new HashSet<Grid2DIndex>());
        for (Grid2DIndex item : newstate.patternA.get(SIMSTEP.ini)) {
            if (actA.action[item.x][item.y] != ICAction.EXTRACT)
                newstate.patternA.get(SIMSTEP.hvt).add(item);
        }
        newstate.patternB.put(SIMSTEP.hvt, new HashSet<Grid2DIndex>());
        for (Grid2DIndex item : newstate.patternB.get(SIMSTEP.ini)) {
            if (actB.action[item.x][item.y] != ICAction.EXTRACT)
                newstate.patternB.get(SIMSTEP.hvt).add(item);
        }

        System.out.println("Harvested");
        newstate.print();

        // 2. 移动
        if (params.spatial == ICConfiguration.NON_SPATIAL) {
            List<Set<Grid2DIndex>> mp = randomDist(
                    newstate.patternA.get(SIMSTEP.hvt).size(),
                    newstate.patternB.get(SIMSTEP.hvt).size());
            newstate.patternA.put(SIMSTEP.mov, mp.get(0));
            newstate.patternB.put(SIMSTEP.mov, mp.get(1));
        } else {
            newstate.patternA.put(SIMSTEP.mov, newstate.patternA.get(SIMSTEP.hvt));
            newstate.patternB.put(SIMSTEP.mov, newstate.patternB.get(SIMSTEP.hvt));
        }

        System.out.println("Moved");
        newstate.print();

        // 3. 死亡&繁殖
        newstate.patternA.put(SIMSTEP.dyn, new HashSet<Grid2DIndex>());
        newstate.patternB.put(SIMSTEP.dyn, new HashSet<Grid2DIndex>());
        for (int x = 0; x < params.K; x++) {
            for (int y = 0; y < params.K; y++) {
                Grid2DIndex idx = new Grid2DIndex(x, y);
                // A
                int[] nsA =  getNeighborState(x, y, params.nghA0, params.nghA1,
                        newstate.patternA.get(SIMSTEP.mov), newstate.patternB.get(SIMSTEP.mov));
                // 未死亡
                if (newstate.patternA.get(SIMSTEP.mov).contains(idx) &&
                        rand.nextDouble() >= params.dRulesA.getValue(nsA[0], nsA[1])) {
                    newstate.patternA.get(SIMSTEP.dyn).add(idx);
                }
                // 繁殖
                if (!(params.coexist == ICConfiguration.COEXIST_NO
                        && newstate.patternB.get(SIMSTEP.mov).contains(idx))
                        && !newstate.patternA.get(SIMSTEP.dyn).contains(idx)
                        && rand.nextDouble() < params.rRulesA.getValue(nsA[0], nsA[1])) {
                    newstate.patternA.get(SIMSTEP.dyn).add(idx);
                }
                // B
                int[] nsB =  getNeighborState(x, y, params.nghB0, params.nghB1,
                        newstate.patternA.get(SIMSTEP.mov), newstate.patternB.get(SIMSTEP.mov));
                // 未死亡
                if (newstate.patternB.get(SIMSTEP.mov).contains(idx) &&
                        rand.nextDouble() >= params.dRulesB.getValue(nsB[0], nsB[1])) {
                    newstate.patternB.get(SIMSTEP.dyn).add(idx);
                }
                // 繁殖
                if (!(params.coexist == ICConfiguration.COEXIST_NO
                        && newstate.patternA.get(SIMSTEP.mov).contains(idx))
                        && !newstate.patternB.get(SIMSTEP.dyn).contains(idx)
                        && rand.nextDouble() < params.rRulesB.getValue(nsB[0], nsB[1])) {
                    if (params.coexist == ICConfiguration.COEXIST_NO
                            && newstate.patternA.get(SIMSTEP.dyn).contains(idx)) {
                        if (rand.nextDouble() >= params.winA) {
                            newstate.patternA.get(SIMSTEP.dyn).remove(idx);
                            newstate.patternB.get(SIMSTEP.dyn).add(idx);
                        }
                    } else {
                        newstate.patternB.get(SIMSTEP.dyn).add(idx);
                    }
                }
            }
        }

        System.out.println("Simulated");
        newstate.print();

        return newstate;
    }
    /*
    public GamePattern simulate(Game group, GamePattern state0, Map<String, GameAction> action) {
        //
        Harvest.dViewers.get(group.id).renew();

        // 前后两个patterns
        ICPattern prestate = (ICPattern) state0;
        ICPattern newstate = new ICPattern();

        // 两种资源分别的行为
        List<String> As = group.getRoleCodes(GamePlayer.ROLE.A);
        List<String> Bs = group.getRoleCodes(GamePlayer.ROLE.B);
        ICAction actA = new ICAction();
        actA.action = new int[params.K][params.K];
        ICAction actB = new ICAction();
        actB.action = new int[params.K][params.K];
        for (String code : action.keySet()) {
            GamePlayer.ROLE role = group.players.get(code).role;
            int w = 0;
            for (int i = 0; i < params.K; i++) {
                for (int j = 0; j < params.K; j++) {
                    ICAction ica = (ICAction) action.get(code);
                    if (ica.action[i][j] == ICAction.EXTRACT) {
                        if (role == GamePlayer.ROLE.A)
                            actA.action[i][j] = ICAction.EXTRACT;
                        else
                            actB.action[i][j] = ICAction.EXTRACT;
                        w++;
                    }
                }
            }
            ICRedistributableBenefit bene = new ICRedistributableBenefit();
            bene.income = w;
            newstate.benefit.put(code, bene);
        }

        // 新的pattern
        newstate.patternA.put(SIMSTEP.ini, prestate.patternA.get(SIMSTEP.dyn));
        newstate.patternB.put(SIMSTEP.ini, prestate.patternB.get(SIMSTEP.dyn));

        // 几个流程
        // 1. 收获
        newstate.patternA.put(SIMSTEP.hvt, new HashSet<Grid2DIndex>());
        for (Grid2DIndex item : newstate.patternA.get(SIMSTEP.ini)) {
            if (actA.action[item.x][item.y] != ICAction.EXTRACT)
                newstate.patternA.get(SIMSTEP.hvt).add(item);
        }
        newstate.patternB.put(SIMSTEP.hvt, new HashSet<Grid2DIndex>());
        for (Grid2DIndex item : newstate.patternB.get(SIMSTEP.ini)) {
            if (actB.action[item.x][item.y] != ICAction.EXTRACT)
                newstate.patternB.get(SIMSTEP.hvt).add(item);
        }

        // 2. 移动
        if (params.spatial == ICConfiguration.NON_SPATIAL) {
            List<Set<Grid2DIndex>> mp = randomDist(
                    newstate.patternA.get(SIMSTEP.hvt).size(),
                    newstate.patternB.get(SIMSTEP.hvt).size());
            newstate.patternA.put(SIMSTEP.mov, mp.get(0));
            newstate.patternB.put(SIMSTEP.mov, mp.get(1));
        } else {
            newstate.patternA.put(SIMSTEP.mov, newstate.patternA.get(SIMSTEP.hvt));
            newstate.patternB.put(SIMSTEP.mov, newstate.patternB.get(SIMSTEP.hvt));
        }

        // 3. 死亡&繁殖
        // A
        Set<Grid2DIndex> survivalA = new HashSet<>();
        Set<Grid2DIndex> newbornA = new HashSet<>();
        for (Grid2DIndex nd : newstate.patternA.get(SIMSTEP.mov)) {
            int[] nsA =  getNeighborState(nd.x, nd.y, params.nghA0, params.nghA1,
                    newstate.patternA.get(SIMSTEP.mov), newstate.patternB.get(SIMSTEP.mov));
            // 死亡
            if (rand.nextDouble() >= calculatorA.dValue(nsA[0], nsA[1]))
                survivalA.add(nd);
            // 繁殖
            Set<Grid2DIndex> exclude = new HashSet<>();
            exclude.addAll(survivalA);
            if (params.coexist == ICConfiguration.COEXIST_NO)
                exclude.addAll(newstate.patternB.get(SIMSTEP.mov));
            Set<Grid2DIndex> available = getNeighbors(nd.x, nd.y, params.areaA0, params.areaA1, exclude);
            double r = calculatorA.rValue(nsA[0], nsA[1]);
            if (params.rtypA == ICConfiguration.REPRODUCING_NUMBER) {
                List<Grid2DIndex> vlist = new ArrayList<>();
                vlist.addAll(available);
                Collections.shuffle(vlist);
                for (int i = 0; i < Math.min(vlist.size(), (int)r); i++)
                    newbornA.add(vlist.get(i));
            } else {
                for (Grid2DIndex idx : available) {
                    if (rand.nextDouble() < r)
                        newbornA.add(idx);
                }
            }
        }
        // B
        Set<Grid2DIndex> survivalB = new HashSet<>();
        Set<Grid2DIndex> newbornB = new HashSet<>();
        for (Grid2DIndex nd : newstate.patternB.get(SIMSTEP.mov)) {
            int[] nsB =  getNeighborState(nd.x, nd.y, params.nghB0, params.nghB1,
                    newstate.patternA.get(SIMSTEP.mov), newstate.patternB.get(SIMSTEP.mov));
            // 死亡
            if (rand.nextDouble() >= calculatorB.dValue(nsB[0], nsB[1]))
                survivalB.add(nd);
            // 繁殖
            Set<Grid2DIndex> exclude = new HashSet<>();
            exclude.addAll(survivalB);
            if (params.coexist == ICConfiguration.COEXIST_NO)
                exclude.addAll(newstate.patternA.get(SIMSTEP.mov));
            Set<Grid2DIndex> available = getNeighbors(nd.x, nd.y, params.areaB0, params.areaB1, exclude);
            double r = calculatorB.rValue(nsB[0], nsB[1]);
            if (params.rtypB == ICConfiguration.REPRODUCING_NUMBER) {
                List<Grid2DIndex> vlist = new ArrayList<>();
                vlist.addAll(available);
                Collections.shuffle(vlist);
                for (int i = 0; i < Math.min(vlist.size(), (int)r); i++)
                    newbornB.add(vlist.get(i));
            } else {
                for (Grid2DIndex idx : available) {
                    if (rand.nextDouble() < r)
                        newbornB.add(idx);
                }
            }
        }
        // 形成最后格局
        System.out.println("Survival A: " + survivalA);
        System.out.println("Survival B: " + survivalB);
        System.out.println("NewBorn A: " + newbornA);
        System.out.println("NewBorn B: " + newbornB);
        newstate.patternA.put(SIMSTEP.dyn, new HashSet<Grid2DIndex>());
        newstate.patternB.put(SIMSTEP.dyn, new HashSet<Grid2DIndex>());
        newstate.patternA.get(SIMSTEP.dyn).addAll(survivalA);
        newstate.patternB.get(SIMSTEP.dyn).addAll(survivalB);
        if (params.coexist == ICConfiguration.COEXIST_NO) {
            for (Grid2DIndex idx : newbornA) {
                if (newbornB.contains(idx)) {
                    if (rand.nextDouble() < params.winA) {
                        newstate.patternA.get(SIMSTEP.dyn).add(idx);
                    } else {
                        newstate.patternB.get(SIMSTEP.dyn).add(idx);
                    }
                    newbornB.remove(idx);
                } else {
                    newstate.patternA.get(SIMSTEP.dyn).add(idx);
                }
            }
            for (Grid2DIndex idx : newbornB) {
                newstate.patternB.get(SIMSTEP.dyn).add(idx);
            }
        } else {
            newstate.patternA.get(SIMSTEP.dyn).addAll(newbornA);
            newstate.patternB.get(SIMSTEP.dyn).addAll(newbornB);
        }

        return newstate;
    }
    */

    private List<Set<Grid2DIndex>> randomDist(int nA, int nB) {
        List<Set<Grid2DIndex>> result = new ArrayList<>(2);
        // 分布A
        Set<Grid2DIndex> ra = new HashSet<>();
        double t = params.K * params.K;
        double a = nA;
        for (int i = 0; i < params.K; i++) {
            for (int j = 0; j < params.K; j++) {
                if (a > 0 && rand.nextDouble() < (a / t)) {
                    ra.add(new Grid2DIndex(i, j));
                    a--;
                }
                t--;
            }
        }
        // 分布B
        Set<Grid2DIndex> rb = new HashSet<>();
        if (params.coexist == ICConfiguration.COEXIST_NO) {
            t = params.K * params.K - nA;
        } else {
            t = params.K * params.K;
        }
        double b = nB;
        for (int i = 0; i < params.K; i++) {
            for (int j = 0; j < params.K; j++) {
                Grid2DIndex idx = new Grid2DIndex(i, j);
                if (params.coexist == ICConfiguration.COEXIST_NO && ra.contains(idx)) {
                    continue;
                }
                if (b > 0 && rand.nextDouble() < (b / t)) {
                    rb.add(idx);
                    b--;
                }
                t--;
            }
        }
        result.add(ra);
        result.add(rb);
        return result;
    }

    private Set<Grid2DIndex> getNeighbors(int x, int y, int area0, int area1, Set<Grid2DIndex> excluded) {
        Set<Grid2DIndex> result = new HashSet<>();
        if (params.boundary == ICConfiguration.BOUNDARY_FIXED) {
            for (int i = Math.max(0, x-area1); i <= Math.min(x+area1, params.K-1); i++) {
                for (int j = Math.max(0, y-area1); j <= Math.min(y+area1, params.K-1); j++) {
                    if (Math.abs(i-x) < area0 && Math.abs(j-y) < area0)
                        continue;
                    Grid2DIndex idx = new Grid2DIndex(i,j);
                    if (!excluded.contains(idx))
                        result.add(idx);
                }
            }
        } else {
            for (int i = -area1; i <= area1; i++) {
                for (int j = -area1; j <= area1; j++) {
                    if (Math.abs(i) < area0 && Math.abs(j) < area0) {
                        continue;
                    }
                    int mi = (x + i + params.K) % params.K;
                    int mj = (y + j + params.K) % params.K;
                    Grid2DIndex idx = new Grid2DIndex(mi, mj);
                    if (!excluded.contains(idx))
                        result.add(idx);
                }
            }
        }
        return result;
    }

    private int[] getNeighborState(int x, int y, int ngh0, int ngh1, Set<Grid2DIndex> resA, Set<Grid2DIndex> resB) {
        int nA = 0;
        int nB = 0;
        if (params.boundary == ICConfiguration.BOUNDARY_FIXED) {
            for (Grid2DIndex idx : resA) {
                int distance = Math.max(Math.abs(idx.x-x), Math.abs(idx.y-y));
                if (distance <= ngh1 && distance >= ngh0) {
                    nA++;
                }
            }
            for (Grid2DIndex idx : resB) {
                int distance = Math.max(Math.abs(idx.x-x), Math.abs(idx.y-y));
                if (distance <= ngh1 && distance >= ngh0) {
                    nB++;
                }
            }
        } else {
            for (Grid2DIndex idx : resA) {
                int distance = Math.max(
                        Math.min(Math.abs(idx.x-x), params.K-Math.abs(idx.x-x)),
                        Math.min(Math.abs(idx.y-y), params.K-Math.abs(idx.y-y)));
                if (distance <= ngh1 && distance >= ngh0) {
                    // System.out.println(idx);
                    nA++;
                }
            }
            for (Grid2DIndex idx : resB) {
                int distance = Math.max(
                        Math.min(Math.abs(idx.x-x), params.K-Math.abs(idx.x-x)),
                        Math.min(Math.abs(idx.y-y), params.K-Math.abs(idx.y-y)));
                if (distance <= ngh1 && distance >= ngh0) {
                    // System.out.println(idx);
                    nB++;
                }
            }
        }
        return new int[]{nA, nB};
    }

    @Override
    public void setParameters(Map<String, String> params) throws Exception {
        super.setParameters(params);
        this.params = (ICParameters) params;
        // segNum = ICConfiguration.MOVEPATTERN.get(this.params.spatial).length + 2; // 一个用于存放初始状态，一个用于存放收获后的状态
        // segNum = (this.params.spatial==ICConfiguration.NO_MOVE)?3:4;
    }

    public String randomName(int range) {
        String str = "";
        String[] arr = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        for(int i = 0; i < range; i++){
            int pos = (int) Math.round(Math.random() * (arr.length - 1));
            str += arr[pos];
        }
        return "CLASS" + str;
    }

    @Override
    public JsonObjectBuilder getJsonParameters() {
        JsonObjectBuilder obj = super.getJsonParameters();
        obj.add("K", params.K);
        obj.add("Unit", params.unit);

        obj.add("chat_emoji", params.chatEmoji);
        obj.add("chat_text", params.chatText);
        obj.add("chat_range", params.chatRange);
        JsonArrayBuilder limarray = Json.createArrayBuilder();
        for (int i = 0; i < params.chatLimit.length; i++) {
            limarray.add(params.chatLimit[i].trim());
        }
        obj.add("chat_limit", limarray);

        obj.add("payoff_unit", params.unit);
        obj.add("payoff_appearance", params.feeAppear);
        obj.add("payoff_a", params.priceA);
        obj.add("payoff_b", params.priceB);
        obj.add("Order", params.spatial);

        /*
        String[] hjs = ICConfiguration.MOVEPATTERN.get(params.spatial);
        JsonArrayBuilder jab = Json.createArrayBuilder();
        jab.add("Initialization");
        jab.add("Harvested");
        for (int i = 0; i < hjs.length; i++) {
            if (hjs[i].isEmpty())
                break;
            else if (hjs[i].equals(ICConfiguration.MP_MOV))
                jab.add("Moved");
            else if (hjs[i].equals(ICConfiguration.MP_REP))
                jab.add("Reproducted");
            else if (hjs[i].equals(ICConfiguration.MP_DIE))
                jab.add("Dead");
        }
        obj.add("Order", jab);
        */

        return obj;
    }
}
