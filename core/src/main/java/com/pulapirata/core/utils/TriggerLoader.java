package com.pulapirata.core.sprites;
import java.util.ArrayList;
import com.pulapirata.core.PetAttributes;
import playn.core.AssetWatcher;
import playn.core.Json;
import playn.core.PlayN;
import playn.core.util.Callback;
import static com.pulapirata.core.utils.Puts.*;

/**
 * Reads game attribute data from a .json file.
 * mimmicks PeaLoader.java
 */
public class TriggerLoader {

    public static void CreateTriggerSet(String path, final double beatsCoelhoHora,
                                        final Callback<PetAttributes> callback) {
        final TriggerSet triggers = new TriggerSet();

        // load the attributes
        PlayN.assets().getText(path, new Callback.Chain<String>(callback) {
            @Override
            public void onSuccess(String resource) {
                // create an asset watcher that will call our callback when all assets
                // are loaded
                AssetWatcher assetWatcher = new AssetWatcher(new AssetWatcher.Listener() {
                    @Override
                    public void done() {
                        callback.onSuccess(attribs);
                    }

                    @Override
                    public void error(Throwable e) {
                        callback.onFailure(e);
                    }
                });

                Json.Object document = PlayN.json().parse(resource);

                // parse the attributes, adding each asset to the asset watcher
                Json.Array jsonTriggers = document.getArray("Attributes");
                for (int i = 0; i < jsonTriggers.length(); i++) {
                    Json.Object jtr = jsonAttributes.getObject(i);
                    dprint("reading name: " + jtr.getString("name"));

                    Modifiers m = new Modifiers();
                    Json.Array jmods = jsonAttributes.getObject(i).getArray("Modifiers");
                    assert jmod != null : "[triggerLoader] required modifiers not found";

                    // set modifiers
                    for (k = 0; k < jmod.length(); ++k) { // for each element in "Modificadores"

                        Json.Object jmatt = jsonStates.getObject(k);
                        for (AttributeID a : AttributeID.values()) {  // for each possible attribute / modifier value
                            // case simple
                            int ai = jmatt.getInt(a.toString());
                            if (ai == 0)
                                dprint("[triggerLoader] Log: modifier for attribute " + a +  " not found, assuming default or jSON comment.");
                            else {
                                m.setValueDelta(a, ai);
                            }
                            //
                            // m.setPassivoDelta(attr, v);

                            // case tipoCoco,
                        }
                    }
                    triggers.get(jtr.getString("name")).set(m);


                    //  XXX DOING ------------------ OK

                    // set agestage
                    Json.Array jas;
                    jas = jsonAttributes.getObject(i).getArray("AgeStage");
                    if (jas == null) {
                        dprint("Tryig Age Stage with space");
                        jas = jsonAttributes.getObject(i).getArray("Age Stage");
                        assert jas != null : "[triggerLoader] required AgeStage not found";
                    }

                    for (AgeStage ass : AgeStage.values())  {
                        int as = jmatt.getString(ass.toString());
                        if (as == 0)
                            dprint("[triggerLoader] Log: age state " + ass +  " NOT blocked or defaulted.");
                        else {
                            if (jmat.getString(as) == "blocked") {
                                triggers.blackList(as);
                            } else {
                                dprint("[triggerLoader] Log: not found blocked for " + ass +  ", assuming blocked.");
                            }
                        }
                    }

                    // ----------
                    Json.Array jsonStates = jsonAttributes.getObject(i).getArray("States");
                    if (jsonStates == null)
                       continue;

                    ArrayList<PetAttributes.State> s = new ArrayList<PetAttributes.State>();
                    ArrayList<Integer> iv = new ArrayList<Integer>();
                    for (int k = 0; k < jsonStates.length(); k++) {
                        Json.Object js = jsonStates.getObject(k);
//                        System.out.println("reading state: " + js.getString("name"));
                        s.add(PetAttributes.State.valueOf(js.getString("name").toUpperCase().replace(' ', '_')));
                        iv.add(js.getInt("max"));
                        assert k != 0 || js.getInt("min") == attribs.get(jatt.getString("name")).min()
                            : "json not consistent with assumption of min of interval equal min of first state";
                    }

                    attribs.sAtt(jatt.getString("name")).set(s, iv);
                }
                attribs.hookupReactiveWires();

                assert attribs.isInitialized() : "not all attributes initialized";

                // start the watcher (it will call the callback when everything is
                // loaded)
                assetWatcher.start();
            }
        });
    }
}
