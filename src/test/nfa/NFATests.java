package test.nfa;

import static org.junit.Assert.*;
import java.util.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

import main.nfa.*;
import main.nfa.NFA.State;
import main.nfa.NFA.Transition;

/**
 * @author Kira Toal
 * JUNIT TESTING LAYOUT BY DR. ANDREW FORNEY
 */
public class NFATests {
    
    // =================================================
    // Test Configuration
    // =================================================
    
    // Global timeout to prevent infinite loops from
    // crashing the test suite
    @Rule
    public Timeout globalTimeout = Timeout.seconds(2);
    
    // Used for grading, reports the total number of tests
    // passed over the total possible
    static int possible = 0, passed = 0;

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            passed++;
        }
    };
    
    @Before
    public void init () {
        possible++;
    }
    
    @AfterClass
    public static void gradeReport () {
        System.out.println("============================");
        System.out.println("Tests Complete");
        System.out.println(passed + " / " + possible + " passed!");
        if ((1.0 * passed / possible) >= 0.9) {
            System.out.println("[!] Nice job!"); // Automated acclaim!
        }
        System.out.println("============================");
    }
    
    // =================================================
    // Unit Tests
    // =================================================

    @Test
    public void testIsDFA_1() {
        // Test behavior when alphabet contains λ.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('0', '1', 'λ')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s1 = new NFA.State("s1", false);
        NFA.State s2 = new NFA.State("s2", true);
        
        myNFA.addState(s1);
        myNFA.addState(s2);
        
        myNFA.addTransition(s1, new NFA.Transition('0', s2));
        myNFA.addTransition(s1, new NFA.Transition('1', s2));
        myNFA.addTransition(s2, new NFA.Transition('0', s2));
        myNFA.addTransition(s2, new NFA.Transition('1', s1));
        
        assertFalse(myNFA.isDFA()); 
    }
    
    @Test
    public void testIsDFA_2() {
        // Test behavior when NFA does not have a transition for every character.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('0', '1')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s1 = new NFA.State("s1", true);
        NFA.State s2 = new NFA.State("s2", false);
        
        myNFA.addState(s1);
        myNFA.addState(s2);
        
        myNFA.addTransition(s1, new NFA.Transition('0', s2));
        myNFA.addTransition(s2, new NFA.Transition('0', s2));
        myNFA.addTransition(s2, new NFA.Transition('1', s1));
        
        assertFalse(myNFA.isDFA()); 
    }

    @Test
    public void testIsDFA_3() {
        // Test behavior when transition has multiple destination states.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('a', 'b')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s1 = new NFA.State("s1", true);
        NFA.State s2 = new NFA.State("s2", false);
        
        myNFA.addState(s1);
        myNFA.addState(s2);
        
        myNFA.addTransition(s1, new NFA.Transition('a', s2));
        myNFA.addTransition(s1, new NFA.Transition('a', s1));
        myNFA.addTransition(s1, new NFA.Transition('b', s2));
        myNFA.addTransition(s2, new NFA.Transition('a', s2));
        myNFA.addTransition(s2, new NFA.Transition('b', s1));
        
        assertFalse(myNFA.isDFA()); 
    }
    
    @Test
    public void testIsDFA_4() {
        // Test isDFA() behavior on valid DFA.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('0', '1', '2')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s1 = new NFA.State("s1", false);
        NFA.State s2 = new NFA.State("s2", true);
        NFA.State s3 = new NFA.State("s3", true);

        
        myNFA.addState(s1);
        myNFA.addState(s2);
        myNFA.addState(s3);
        
        myNFA.addTransition(s1, new NFA.Transition('0', s1));
        myNFA.addTransition(s1, new NFA.Transition('1', s1));
        myNFA.addTransition(s1, new NFA.Transition('2', s2));
        myNFA.addTransition(s2, new NFA.Transition('0', s2));
        myNFA.addTransition(s2, new NFA.Transition('1', s3));
        myNFA.addTransition(s2, new NFA.Transition('2', s2));
        myNFA.addTransition(s3, new NFA.Transition('0', s1));
        myNFA.addTransition(s3, new NFA.Transition('1', s2));
        myNFA.addTransition(s3, new NFA.Transition('2', s2));
        
        assertTrue(myNFA.isDFA()); 
    }   
    
    @Test 
    public void testAddTransition_1() {
        // Do not add duplicate transition.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('a', 'b')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s1 = new NFA.State("s1", true);
        NFA.State s2 = new NFA.State("s2", false);
        
        myNFA.addState(s1);
        myNFA.addState(s2);
        
        myNFA.addTransition(s1, new NFA.Transition('a', s2));
        myNFA.addTransition(s1, new NFA.Transition('a', s2)); // Attempt to add duplicate.
        myNFA.addTransition(s1, new NFA.Transition('b', s2));
        myNFA.addTransition(s2, new NFA.Transition('a', s2));
        myNFA.addTransition(s2, new NFA.Transition('b', s1));
        
        assertTrue(myNFA.isDFA()); 
    }
    
    @Test
    public void testKEquivalenceHelper_t0() {
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('0', '1')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s0 = new NFA.State("s0", false);
        NFA.State s1 = new NFA.State("s1", false);
        NFA.State s2 = new NFA.State("s2", false);
        NFA.State s3 = new NFA.State("s3", false);
        NFA.State s4 = new NFA.State("s4", true);
      
        myNFA.addState(s0);
        myNFA.addState(s1);
        myNFA.addState(s2);
        myNFA.addState(s3);
        myNFA.addState(s4);
        
        myNFA.addTransition(s0, new NFA.Transition('0', s1));
        myNFA.addTransition(s0, new NFA.Transition('1', s3));
        myNFA.addTransition(s1, new NFA.Transition('0', s2));
        myNFA.addTransition(s1, new NFA.Transition('1', s4));
        myNFA.addTransition(s2, new NFA.Transition('0', s1));
        myNFA.addTransition(s2, new NFA.Transition('1', s4));
        myNFA.addTransition(s3, new NFA.Transition('0', s2));
        myNFA.addTransition(s3, new NFA.Transition('1', s4));
        myNFA.addTransition(s4, new NFA.Transition('0', s4));
        myNFA.addTransition(s4, new NFA.Transition('1', s4));
        
        HashSet<HashSet<State>> stateGroups = new HashSet<HashSet<State>>();
        HashSet<State> acceptStates = new HashSet<State>(Arrays.asList(s4));
        HashSet<State> nonAcceptStates = new HashSet<State>(Arrays.asList(s0, s1, s2, s3)); 
        stateGroups.add(acceptStates);
        stateGroups.add(nonAcceptStates);
        
        var expectedAnswer = new HashSet<HashSet<State>>();
        expectedAnswer.add(new HashSet<State>(Arrays.asList(s4)));
        expectedAnswer.add(new HashSet<State>(Arrays.asList(s0)));
        expectedAnswer.add(new HashSet<State>(Arrays.asList(s3, s1, s2)));
         
        assertEquals(myNFA.kEquivalenceHelper(stateGroups), expectedAnswer);               
    }
    
    @Test
    public void testMinimize_t0() {
        // Test minimize() method on already minimized DFA.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('0', '1')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s0 = new NFA.State("s0", false);
        NFA.State s1 = new NFA.State("s1", true);
        
        myNFA.addState(s0);
        myNFA.addState(s1);
        
        myNFA.addTransition(s0, new NFA.Transition('0', s1));
        myNFA.addTransition(s0, new NFA.Transition('1', s0));
        myNFA.addTransition(s1, new NFA.Transition('0', s0));
        myNFA.addTransition(s1, new NFA.Transition('1', s1));
        
        NFA DFA = myNFA.minimize();
        
        assertEquals(DFA.getData().get(s0).get('0'), new HashSet<>(Arrays.asList(s1)));
    }
    
    @Test
    public void testMinimize_t1() {
        // Test minimize() method on non minimized DFA.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('0', '1')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s0 = new NFA.State("s0", false);
        NFA.State s1 = new NFA.State("s1", false);
        
        myNFA.addState(s0);
        myNFA.addState(s1);
        
        myNFA.addTransition(s0, new NFA.Transition('0', s1));
        myNFA.addTransition(s0, new NFA.Transition('1', s0));
        myNFA.addTransition(s1, new NFA.Transition('0', s0));
        myNFA.addTransition(s1, new NFA.Transition('1', s1));
        
        NFA DFA = myNFA.minimize();
        
        NFA expected = new NFA(new HashSet<>(Arrays.asList('0', '1')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s0s1 = new NFA.State("s0s1", false);
        expected.addState(s0s1);
        expected.addTransition(s0s1, new NFA.Transition('0', s0s1));
        expected.addTransition(s0s1, new NFA.Transition('1', s0s1));
        
        assertEquals(DFA.getAlphabet(), expected.getAlphabet());
        assertEquals(DFA.getData().size(), expected.getData().size());
    }

    @Test
    public void testMinimize_t2() {
        // Test minimize() method on more complex DFA.
        NFA myNFA = new NFA(new HashSet<>(Arrays.asList('0', '1')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s0 = new NFA.State("s0", false);
        NFA.State s1 = new NFA.State("s1", false);
        NFA.State s2 = new NFA.State("s2", false);
        NFA.State s3 = new NFA.State("s3", false);
        NFA.State s4 = new NFA.State("s4", true);
        
        myNFA.addState(s0);
        myNFA.addState(s1);
        myNFA.addState(s2);
        myNFA.addState(s3);
        myNFA.addState(s4);
        
        myNFA.addTransition(s0, new NFA.Transition('0', s1));
        myNFA.addTransition(s0, new NFA.Transition('1', s3));
        myNFA.addTransition(s1, new NFA.Transition('0', s2));
        myNFA.addTransition(s1, new NFA.Transition('1', s4));
        myNFA.addTransition(s2, new NFA.Transition('0', s1));
        myNFA.addTransition(s2, new NFA.Transition('1', s4));
        myNFA.addTransition(s3, new NFA.Transition('0', s2));
        myNFA.addTransition(s3, new NFA.Transition('1', s4));
        myNFA.addTransition(s4, new NFA.Transition('0', s4));
        myNFA.addTransition(s4, new NFA.Transition('1', s4));
        
        NFA DFA = myNFA.minimize();
        
        NFA expected = new NFA(new HashSet<>(Arrays.asList('0', '1')), new HashMap<State, HashMap<Character, HashSet<State>>>());
        NFA.State s1s2s3 = new NFA.State("s1s2s3", false);

        expected.addState(s0);
        expected.addState(s1s2s3);
        expected.addState(s4);

        expected.addTransition(s0, new NFA.Transition('0', s1s2s3));
        expected.addTransition(s0, new NFA.Transition('1', s1s2s3));
        expected.addTransition(s1s2s3, new NFA.Transition('0', s1s2s3));
        expected.addTransition(s1s2s3, new NFA.Transition('1', s4));
        expected.addTransition(s4, new NFA.Transition('0', s4));
        expected.addTransition(s4, new NFA.Transition('1', s4));
        
        assertEquals(DFA.getAlphabet(), expected.getAlphabet());
        assertEquals(DFA.getData().size(), expected.getData().size());
        
        for (var state: DFA.getData().keySet()) {
            if (state.getName().equals("s4")) {
                assertEquals(DFA.getData().get(state).get('0').toArray(new State[1])[0].getName(), "s4");
                assertEquals(DFA.getData().get(state).get('1').toArray(new State[1])[0].getName(), "s4");
            }
        }
    }   
}

