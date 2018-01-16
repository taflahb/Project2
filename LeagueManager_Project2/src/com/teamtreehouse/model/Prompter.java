package com.teamtreehouse.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Prompter {
    private List<Player> mPlayers;
    private List<Team> mTeams;
    private BufferedReader mReader;
    private Map<Integer, String> mMenu;

    public Prompter(Player[] players) {
        mPlayers = new ArrayList<>();
        Collections.addAll(mPlayers, players);
        Collections.sort(mPlayers);
        mTeams = new ArrayList<>();
        mReader = new BufferedReader(new InputStreamReader(System.in));
        mMenu = new HashMap<Integer, String>();
        mMenu.put(1, "Create a new team");
        mMenu.put(2, "Add a player to a team");
        mMenu.put(3, "Remove a player from a team");
        mMenu.put(4, "View report of a team grouped by height");
        mMenu.put(5, "View League Balance Report");
        mMenu.put(6, "Print out a team roster");
        mMenu.put(7, "Exit the program");
    }

    private int promptAction() throws IOException {
        System.out.println("Your options are:");
        for (Map.Entry<Integer, String> option : mMenu.entrySet()) {
            System.out.printf("%d - %s %n",
                                option.getKey(),
                                option.getValue());
        }
        System.out.print("\u001B[31m \nWhat do you want to do:  ");
        int choice = Integer.parseInt(mReader.readLine().trim());
        return choice;
    }

    public void run() {
        int choice = 0;
        do {
            try {
                choice = promptAction();
                switch (choice) {
                    case 1:
                        if (mTeams.size() >= 3) {
                            System.out.println("\n\n \u001B[31m Sorry, the team limit has been reached - no new teams may be created.\n\n\n");
                            break;
                        }
                        createTeam();
                        break;
                    case 2:
                        Team team = promptForTeam();
                        if (team.getSize()>=11) {
                            System.out.println("\u001B[31m \n Team already consists of Maximum.\n\n");
                            break;
                        }
                        Player player = promptForPlayer();
                        team.addPlayer(player);
                        System.out.printf("\u001B[31m %nAdded %s to team %s.%n%n", player.getPlayerInfo(), team.getTeamName());
                        break;
                    case 3:
                        team = promptForTeam();
                        player = promptByTeam(team);
                        team.removePlayer(player);
                        System.out.printf("\u001B[31m %nRemoved %s from the team %s.%n%n", player.getPlayerInfo(), team.getTeamName());
                        break;
                    case 4:
                        team = promptForTeam();
                        heightReport(team);
                        break;
                    case 5:
                          balanceReport();
                        break;
                    case 6:
                        team = promptForTeam();
                        printRoster(team);
                        break;
                    case 7:
                        System.out.println("Nice To Meet You");
                        break;
                    default:
                        System.out.printf("\u001B[31m %nUnknown choice:  '%s'. Try again.  %n%n",
                                choice);
                }
            } catch (IOException ioe) {
                System.out.println("%nProblem with input%n%n");
                ioe.printStackTrace();
            }
        } while (choice!=7);
    }

    public void createTeam() throws IOException {
        System.out.print("Please enter a new team name: ");
        String teamName = mReader.readLine();
        System.out.print("Please enter the coach's name for the new team: ");
        String coachName = mReader.readLine();
        Team team = new Team(teamName, coachName);
        mTeams.add(team);
        Collections.sort(mTeams);
        System.out.printf("\u001B[31m  %s Created with Coach %s %n%n", teamName, coachName);
    }

    private int promptForTeamIndex(List<Team> teams) throws IOException {
        int counter = 1;
        int choice = -1;
        for (Team team : teams) {
            System.out.printf("%d.)  %s %n", counter, team.getTeamName());
            counter++;
        }

        do {
            try {
                System.out.print("Select the team:  ");
                String optionAsString = mReader.readLine();
                choice = Integer.parseInt(optionAsString.trim());
            } catch (NumberFormatException e) {
                System.out.println("\u001B[31m Invalid input.  Please enter a number.");
            }

        } while (choice > teams.size() || choice < 1);

        return choice - 1;
    }

    private int promptForPlayerIndex(List<Player> players) throws IOException {
        int counter = 1;
        int choice = -1;
        for (Player player : players) {
            System.out.printf("%d.)  %s %n", counter, player.getPlayerInfo());
            counter++;
        }

        do {
            try {
                System.out.printf("Select a player:  ");
                String optionAsString = mReader.readLine();
                choice = Integer.parseInt(optionAsString.trim());
            } catch (NumberFormatException e) {
                System.out.println("\u001B[31m Invalid input.  Please enter a number.");
            }
        } while (choice > players.size() || choice < 1);

        return choice - 1;
    }

    private Team promptForTeam() throws IOException {
        int index = promptForTeamIndex(mTeams);
        return mTeams.get(index);
    }

    public Player promptForPlayer() throws IOException {
        int index = promptForPlayerIndex(getPlayers());
        return getPlayers().get(index);
    }

    public Player promptByTeam(Team team) throws IOException {
        int index = promptForPlayerIndex(team.getAllPlayers());
        return team.getAllPlayers().get(index);
    }

    public void heightReport(Team team) {
        Collections.sort(team.getAllPlayers(), new Comparator<Player>(){

            @Override
            public int compare(Player o1, Player o2) {
                return Integer.compare(o1.getHeightInInches(), o2.getHeightInInches());
            }
        });

        System.out.printf("Height report for %s%n%n", team.getTeamName());
        int counter = 1;
        for (Player player : team.getAllPlayers()) {
            System.out.printf("%d.)  %s %n", counter, player.getPlayerInfo());
            counter++;
        }
        System.out.println();
    }

        public void balanceReport() {
            System.out.println("League Balance Report\n");
            Map<Team, Integer> experiencedPlayerCounts = new HashMap<Team, Integer>();

            for (Team team : mTeams) {
                int exp = 0;
                for (Player player : team.getAllPlayers()) {
                    if (player.isPreviousExperience()) {
                        exp++;
                    }
                }
                experiencedPlayerCounts.put(team, exp);
            }

            for (Team team : mTeams) {
                int experiencedCount = experiencedPlayerCounts.get(team);
                int inexperiencedCount = team.getSize() - experiencedCount;
                float avg = (((float) experiencedCount / team.getSize()) * 100);
                System.out.printf("%s, Experienced Players: %d, Inexperienced Players: %d, " +
                                "Average Experience Level: %.1f%%%n",
                                team.getTeamName(), experiencedCount, inexperiencedCount, avg);
            }

            Map<Integer, Integer> countsByHeight = new HashMap<Integer, Integer>();
            for (Team team : mTeams) {
                int counter = 0;
                for (Player player : team.getAllPlayers()) {
                    int heightValue = player.getHeightInInches();
                    Integer heightCounts = countsByHeight.get(heightValue);
                    if (heightCounts == null) {
                        heightCounts = 0;
                    }
                    heightCounts++;
                    countsByHeight.put(heightValue, heightCounts);
                }

                for (Map.Entry<Integer, Integer> entry : countsByHeight.entrySet()) {
                    System.out.printf("%nTeam: %s  Height: %d  Number of Players: %d%n", team.getTeamName(), entry.getKey(), entry.getValue());
                }
            }
            System.out.println();
        }

        public void printRoster(Team team){
            System.out.printf("Team roster for %s%n%n", team.getTeamName());
            int counter = 1;
            for (Player player : team.getAllPlayers()) {
                System.out.printf("%d.)  %s %n", counter, player.getPlayerInfo());
                counter++;
            }
            System.out.println();
        }


        public List<Player> getPlayers() {
            Set<Player> getPlayers = new TreeSet<>();
            getPlayers.addAll(mPlayers);
            for (Team team : mTeams) {
                getPlayers.removeAll(team.getAllPlayers());
            }
            return new ArrayList<Player>(getPlayers);
        }
}