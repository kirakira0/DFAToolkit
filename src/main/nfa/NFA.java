/**
 *
 * @author Kira Toal
 */ 

package main.nfa;

import java.util.HashSet;

import main.nfa.NFA.State;

import java.util.Arrays;
import java.util.HashMap;

public class NFA {

    private HashSet<Character> alphabet; 
    private HashMap<State, HashMap<Character, HashSet<State>>> data; 


    public NFA(HashSet<Character> alphabet, HashMap<State, HashMap<Character, HashSet<State>>> data) {
        this.alphabet = alphabet;
        this.data = data;
    }
    
    public void addState(State state) {
        if (this.data.containsKey(state)) {
            throw new IllegalArgumentException("State already exists."); 
        }
        this.data.put(state, new HashMap<Character, HashSet<State>>());         
    }
    
    
    public void addTransition(State state, Transition transition) {
        // Ensure symbol in alphabet. 
        if (!this.alphabet.contains(transition.symbol)) {
            throw new IllegalArgumentException("Transition symbol not in alphabet.");                         
        }
        // Ensure state in NFA. 
        if (!this.data.containsKey(state)) {
            throw new IllegalArgumentException("Cannot add tranistion to nonexistant state.");             
        }
        // Add new transition. 
        if (!this.data.get(state).containsKey(transition.symbol)) {
            this.data.get(state).put(transition.symbol, new HashSet<State>(Arrays.asList(transition.dest)));
        }
        // Add state to transition.
        else {
            // Do not add duplicate transition. 
            if (this.data.get(state).get(transition.symbol).contains(transition.dest)) {
                return;
            }
            HashSet<State> oldStatesSet = this.data.get(state).get(transition.symbol);
            oldStatesSet.add(transition.dest);         
        }                  

    }
    
    /**
     * Determined whether an NFA is a DFA. In order for this to be true,
     * the following conditions must be satisfied: 
     *  (1) The alphabet may not contain λ (no λ transitions allowed)/
     *  (2) Each state must have a transition for every character in the alphabet.
     *  (3) Every symbol must have only one destination state.
     * @return Whether or not a given NFA is also an DFA.
     */
    public boolean isDFA() {
        // Condition 1: DFA cannot use lambda moves. 
        if (this.alphabet.contains('λ')) {return false;}
        for (State state: this.data.keySet()) {
            for (Character c: this.alphabet) {
                // Condition 2: DFA must have transition for every char
                //  in the alphabet. 
                if (!this.data.get(state).keySet().contains(c)) {
                    return false; 
                }
            }
            for (Character symbol: this.data.get(state).keySet()) {
                 // Condition 3: Each transition symbol must have only one 
                 //   destination state.
                 if (this.data.get(state).get(symbol).size() > 1) {
                     return false;
                 }         
             }                   
        }       
        return true;       
    }
    
    /**
     * 
     */
    public NFA minimize() {
        // K equivalence 
        var stateGroups = new HashSet<HashSet<State>>();
        var acceptStates = new HashSet<State>();
        var nonAcceptStates = new HashSet<State>();
        for (State state: this.data.keySet()) {
            if (state.accept)  {acceptStates.add(state);} 
            else {nonAcceptStates.add(state);}
        }
        stateGroups.add(acceptStates);
        stateGroups.add(nonAcceptStates);
        
        var sortedGroups = this.kEquivalenceHelper(stateGroups); 
        
        NFA DFA = new NFA(this.alphabet, new HashMap<State, HashMap<Character, HashSet<State>>>());
        for (var group: stateGroups) {
            
        }

        return DFA;      

    }

    
    /**
     * Sort the states of the DFA into groups using the k equivalence method. 
     * @param stateGroups The states should originally be split into two groups:
     *      one for the accept states and one for the non-accept states. 
     * @return The correct sorting of the groups. 
     */
    public HashSet<HashSet<State>> kEquivalenceHelper(HashSet<HashSet<State>> stateGroups) {
        var newGroups = new HashSet<HashSet<State>>();
        for (var group: stateGroups) {
            var keep = new HashSet<State>();
            var leave = new HashSet<State>();
            for (var state: group) {
                if (this.stateExits(state, group)) {
                    leave.add(state);
                } else {
                    keep.add(state);
                }
            }
            if (!leave.isEmpty()) {
                newGroups.add(leave);
            }
            if (!keep.isEmpty()) {
                newGroups.add(keep);
            }
        }
        // this.printStateGroup(newGroups);
        return stateGroups.equals(newGroups) ? newGroups : this.kEquivalenceHelper(newGroups);       
    }
    
    /**
     * 
     * @param groups
     */
    private void printStateGroup(HashSet<HashSet<State>> groups) {
        String result = "Groups: ";
        for (HashSet<State> group: groups) {
            result += "[ ";
            for (State state: group) {
                result += " " + state.name + " ";
            }
            result += " ]";
        }
        System.out.println(result);       
    }
    
    /**
     * 
     * @param state
     * @param group
     * @return
     */
    private boolean stateExits(State state, HashSet<State> group) {
        for (Character symbol: this.data.get(state).keySet()) {
            for (State destination: this.data.get(state).get(symbol)) {
                if (!group.contains(destination) && group.size() > 1) {                                                        
                    return true;
                }
            }            
        }
        return false;
    }
    
    
    
    // METHOD TO COMBINE STATESET INTO SINGLE STATE
//    public DFA constructMinimizedDFA() {
//        
//        
//    }
    
    
    @Override 
    public String toString() {
        String result = "----------------NFA-------------------";
        for (State state: this.data.keySet()) {
            System.out.println();
            result += "\n" + state.name + "\t"; 
            for (Character symbol: this.data.get(state).keySet()) {
                result += "[" + symbol + ":" + this.transitionSetToString(this.data.get(state).get(symbol)) + "]";
            }   
        }        
        result += "\n" + "--------------------------------------";
        return result;
    }
    
    /**
     * 
     */
    private String transitionSetToString(HashSet<State> states) {
        String result = ""; 
        for (State state: states) {
            result += " " + state.name;           
        }
        return result;
    } 
          
    /**
     * 
     *
     */
    public static class State {

        private String name;
        private boolean accept;        
                
        public State(String name, boolean accept) {
            this.name = name;
            this.accept = accept;
        }        
    }
    
    /**
     * 
     *
     */
    public static class Transition {
        
        private Character symbol;
        private State dest; 
        
        public Transition(Character symbol, State dest) {
            this.symbol = symbol; 
            this.dest = dest;
        }       
    }    
}