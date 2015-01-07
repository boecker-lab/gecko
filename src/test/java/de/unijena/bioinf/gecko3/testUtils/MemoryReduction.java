/*
 * Copyright 2014 Sascha Winter
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.testUtils;

import java.util.ArrayList;
import java.util.List;

/**
* @author Sascha Winter (sascha.winter@uni-jena.de)
*/
public class MemoryReduction {
    List<Integer> id;
    List<Integer> anzahl;
    List<Integer> newId;

    public MemoryReduction(){
        this.anzahl = new ArrayList<>();
        this.id = new ArrayList<>();
        this.newId = new ArrayList<>();
    }

    public void anlegen(int x){
        this.id.add(x);
        this.anzahl.add(1);
        this.newId.add(0);
    }

    private void hinzufuegen(int x){
        this.anzahl.set(this.id.indexOf(x), (this.anzahl.get(this.id.indexOf(x))+1));
    }

    public void suche(int x){
        if (x > 0){
            if(this.id.contains(x)){
                hinzufuegen(x);
            } else {
                anlegen(x);
            }
        } else {
            if (this.id.contains(x) != true){
                anlegen(x);
            }
        }
    }

    public int[] umschreiben(){
        int[] y = new int[this.id.size()];
        for(int i=0;i<this.id.size();i++){
            y[i]=this.id.get(i);
        }

        return y;
    }

    public static void memReducer(int[][][] genomes, int[][][] genomes2){
        MemoryReduction help = new MemoryReduction();

        for(int l = 0; l<genomes.length;l++){
            for(int m = 0; m<genomes[l].length; m++){
                for(int x = 0; x<genomes[l][m].length;x++){
                    help.suche(genomes[l][m][x]);
                }
            }
        }

        int count = 1;

        for(int l = 0; l<genomes.length;l++){
            for(int m = 0; m<genomes[l].length; m++){
                MemoryReduction help2 = new MemoryReduction();
                for(int x = 0; x<genomes[l][m].length;x++){
                    int k = help.id.indexOf(genomes[l][m][x]);
                    if(help.anzahl.get(k)>1){
                        if (help.newId.get(k) == 0){
                            help.newId.set(k, count);
                            count++;
                        }
                        help2.anlegen(help.newId.get(k));
                    } else if(help2.id.isEmpty() != true) {
                        if(genomes[l][m][x]==0){
                            help2.anlegen(0);
                        } else {
                            help2.anlegen(-1);
                        }
                    } else {
                        help2.anlegen(0);
                    }
                }
                genomes[l][m] = help2.umschreiben();
            }
        }
        
        for(int l = 0; l<genomes2.length;l++){
            for(int m = 0; m<genomes2[l].length; m++){
                MemoryReduction help2 = new MemoryReduction();
                for(int x = 0; x<genomes2[l][m].length;x++){
                    int k = help.id.indexOf(genomes2[l][m][x]);
                    if(help.anzahl.get(k)>1){
                        if (help.newId.get(k) == 0){
                            help.newId.set(k, count);
                            count++;
                        }
                        help2.anlegen(help.newId.get(k));
                    } else if(help2.id.isEmpty() != true) {
                        if(genomes[l][m][x]==0){
                            help2.anlegen(0);
                        } else {
                            help2.anlegen(-1);
                        }
                    } else {
                        help2.anlegen(0);
                    }
                }
                genomes2[l][m] = help2.umschreiben();
            }
        }

    }
    
    
    public static void memReducer(int[][][] genomes, ExpectedReferenceClusterValues[] expectedReferenceClusters){
        MemoryReduction help = new MemoryReduction();

        for(int l = 0; l<genomes.length;l++){
            for(int m = 0; m<genomes[l].length; m++){
                for(int x = 0; x<genomes[l][m].length;x++){
                    help.suche(genomes[l][m][x]);
                }
            }
        }

        int count = 1;

        for(int l = 0; l<genomes.length;l++){
            for(int m = 0; m<genomes[l].length; m++){
                MemoryReduction help2 = new MemoryReduction();
                for(int x = 0; x<genomes[l][m].length;x++){
                    int k = help.id.indexOf(genomes[l][m][x]);
                    if(help.anzahl.get(k)>1){
                        if (help.newId.get(k) == 0){
                            help.newId.set(k, count);
                            count++;
                        }
                        help2.anlegen(help.newId.get(k));
                    } else if(help2.id.isEmpty() != true) {
                        if(genomes[l][m][x]==0){
                            help2.anlegen(0);
                        } else {
                            help2.anlegen(-1);
                        }
                    } else {
                        help2.anlegen(0);
                    }
                }
                genomes[l][m] = help2.umschreiben();
            }
        }

        for (ExpectedReferenceClusterValues values : expectedReferenceClusters){
            List<Integer> genes = values.getGeneContent();
            for (int l=0;l<genes.size();l++){
                int index = help.id.indexOf(genes.get(l));
                if (help.anzahl.get(index)>1){
                    if(help.newId.get(index) == 0){
                        genes.set(l, count);
                        count++;
                    } else if (genes.get(l)>0){
                        genes.set(l, help.newId.get(index));
                    } else {
                    }
                } else {
                    genes.set(l, -1);
                }
            }
        }

        //return genomes;
    }
}
