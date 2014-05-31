package forge.match.input;

import com.google.common.collect.Iterables;

import forge.GuiBase;
import forge.game.GameEntity;
import forge.game.card.Card;

import java.util.Collection;

public abstract class InputSelectManyBase<T extends GameEntity> extends InputSyncronizedBase {
    private static final long serialVersionUID = -2305549394512889450L;

    protected boolean bCancelled = false;
    protected final int min;
    protected final int max;
    protected boolean allowUnselect = false;
    protected boolean allowCancel = false;

    protected String message = "Source-Card-Name - Select %d more card(s)";

    protected InputSelectManyBase(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min must not be greater than Max");
        }
        this.min = min;
        this.max = max;
    }

    protected void refresh() {
        if (hasAllTargets()) {
            selectButtonOK();
        }
        else {
            this.showMessage();
        }
    }

    protected abstract boolean hasEnoughTargets();
    protected abstract boolean hasAllTargets();

    protected abstract String getMessage();

    @Override
    public final void showMessage() {
        showMessage(getMessage());

        boolean canCancel = allowCancel;
        boolean canOk = hasEnoughTargets();

        ButtonUtil.reset();
        if (canOk && canCancel) { ButtonUtil.enableAllFocusOk(); }
        if (!canOk && canCancel) { ButtonUtil.enableOnlyCancel(); }
        if (canOk && !canCancel) { ButtonUtil.enableOnlyOk(); }
        if (!canOk && !canCancel) { ButtonUtil.disableAll(); }
    }


    @Override
    protected final void onCancel() {
        bCancelled = true;
        this.getSelected().clear();
        this.stop();
        afterStop();
    }

    public final boolean hasCancelled() {
        return bCancelled;
    }

    public abstract Collection<T> getSelected();
    public T getFirstSelected() { return Iterables.getFirst(getSelected(), null); }
    
    @Override
    protected final void onOk() {
        this.stop();
        afterStop();
    }

    public void setMessage(String message0) {
        this.message = message0;
    }

    protected void onSelectStateChanged(GameEntity c, boolean newState) {
        if (c instanceof Card) {
            GuiBase.getInterface().setUsedToPay((Card)c, newState); // UI supports card highlighting though this abstraction-breaking mechanism
        }
    }

    protected void afterStop() {
        for (GameEntity c : getSelected()) {
            if (c instanceof Card) {
                GuiBase.getInterface().setUsedToPay((Card)c, false);
            }
        }
    }

    public final boolean isUnselectAllowed() { return allowUnselect; }
    public final void setUnselectAllowed(boolean allow) { this.allowUnselect = allow; }

    public final void setCancelAllowed(boolean allow) { this.allowCancel = allow ; }
}
