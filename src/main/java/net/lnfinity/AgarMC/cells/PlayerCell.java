package net.lnfinity.AgarMC.cells;

import net.lnfinity.AgarMC.AgarMC;
import net.lnfinity.AgarMC.cells.core.Cell;
import net.lnfinity.AgarMC.game.CPlayer;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

public class PlayerCell extends Cell {

	private final ArmorStand saddle;
	private final CPlayer player;
	private boolean isDriver;
	protected boolean canMerge = true;
	protected boolean action = false;
	
	public PlayerCell(CPlayer player, int mass, double x, double y) {
		this(player, mass, x, y, true);
	}
	
	public PlayerCell(CPlayer player, int mass, double x, double y, boolean drive) {
		super(mass, x, y);
		
		if(drive)
			player.getPlayer().teleport(new Location(AgarMC.get().getWorld(), x, 128, y));
		
		saddle = AgarMC.get().getWorld().spawn(new Location(AgarMC.get().getWorld(), x, 128, y), ArmorStand.class);
		saddle.setVisible(false);
		saddle.setSmall(true);
		slime.setPassenger(saddle);
		
		this.player = player;
		
		isDriver = drive;
		
		AgarMC.get().getServer().getScheduler().runTaskLater(AgarMC.get(), new Runnable() {
			@Override
			public void run() {
				action = true;
			}
		}, 20L);
	}

	@Override
	public void remove() {
		super.remove();
		
		saddle.remove();
	}
	
	@Override
	public void recalculateSize() {
		super.recalculateSize();
		
		if(player != null)
			player.massChanged();
	}

	public boolean isDriving() {
		return isDriver;
	}

	public void setDriving(boolean driving) {
		this.isDriver = driving;
		saddle.setPassenger(isDriver ? player.getPlayer() : null);
	}

	public boolean canMerge() {
		return canMerge;
	}

	public void setCanMerge(boolean canMerge) {
		this.canMerge = canMerge;
	}
	
	public CPlayer getOwner() {
		return player;
	}
	
	@Override
	public void move(double x, double y) {
		super.move(x, y);
		
		if(isDriver && saddle.getPassenger() == null) {
			saddle.setPassenger(player.getPlayer());
		}
	}
	
	public void ejectMass() {
		if(!action) return;
		if(mass >= 30) {
			mass -= 10;
			final StaticCell cell = new StaticCell(8, getX(), getY());
			cell.setInvinsible(true);
			Vector vector = player.getPlayer().getLocation().getDirection().setY(0).normalize().multiply(2);
			cell.setVelocity(vector);
			AgarMC.get().getGame().addStaticCell(cell);
			
			AgarMC.get().getServer().getScheduler().runTaskLater(AgarMC.get(), new Runnable() {
				@Override
				public void run() {
					cell.setInvinsible(false);
				}
			}, 10L);
			
			recalculateSize();
		}
	}
	
	public void split() {
		if(!action) return;
		if(mass >= 30) {
			mass = mass / 2 - 1;
			final PlayerCell cell = new PlayerCell(player, mass, getX(), getY(), false);
			Vector vector = player.getPlayer().getLocation().getDirection().setY(0).normalize().multiply(5);
			cell.setVelocity(vector);
			cell.setCanMerge(false);
			player.addCell(cell);
			
			AgarMC.get().getServer().getScheduler().runTaskLater(AgarMC.get(), new Runnable() {
				@Override
				public void run() {
					cell.setInvinsible(false);
				}
			}, 10 * 20L);
			
			recalculateSize();
		}
	}
}