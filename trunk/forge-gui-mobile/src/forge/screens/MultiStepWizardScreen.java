package forge.screens;

import forge.Forge;
import forge.menu.FPopupMenu;
import forge.screens.match.views.VPrompt;
import forge.toolbox.FContainer;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;

public abstract class MultiStepWizardScreen<T> extends FScreen {
    protected final WizardStep<T>[] steps;
    protected final T model;
    private WizardStep<T> currentStep;
    private final VPrompt prompt = add(new VPrompt("Back", "Next", new FEventHandler() {
        @Override
        public void handleEvent(FEvent e) {
            advanceStep(-1);
        }
    }, new FEventHandler() {
        @Override
        public void handleEvent(FEvent e) {
            advanceStep(1);
        }
    }));

    protected MultiStepWizardScreen(String headerCaption, WizardStep<T>[] steps0, T model0) {
        super(headerCaption);
        steps = steps0;
        model = model0;
        initialize();
    }
    protected MultiStepWizardScreen(String headerCaption, FPopupMenu menu, WizardStep<T>[] steps0, T model0) {
        super(headerCaption, menu);
        steps = steps0;
        model = model0;
        initialize();
    }
    protected MultiStepWizardScreen(Header header0, WizardStep<T>[] steps0, T model0) {
        super(header0);
        steps = steps0;
        model = model0;
        initialize();
    }

    private void initialize() {
        int index = 0;
        for (WizardStep<T> step : steps) {
            step.index = index++;
            step.parentScreen = this;
            add(step);
            step.setVisible(false);
        }
        prompt.getBtnCancel().setEnabled(true);
        setCurrentStep(0);
    }

    private void advanceStep(int dir) {
        int newIndex = currentStep.index + dir;
        if (newIndex < 0) { return; }

        if (dir > 0) {
            if (!currentStep.updateModelAndAdvance(model)) { return; }
    
            if (newIndex >= steps.length) {
                finish();
                return;
            }
        }

        setCurrentStep(newIndex);
    }

    private void setCurrentStep(int index) {
        if (currentStep != null) {
            if (currentStep.index == index) { return; }

            currentStep.setVisible(false);
            if (currentStep.index == steps.length - 1) {
                prompt.getBtnCancel().setText("Next");
            }
        }

        currentStep = steps[index];

        currentStep.setVisible(true);
        prompt.getBtnOk().setEnabled(index > 0);
        if (index == steps.length - 1) {
            prompt.getBtnCancel().setText("Finish");
        }
        prompt.setMessage(currentStep.getMessage());

        if (Forge.getCurrentScreen() == this) {
            currentStep.onActivate(model);
        }
    }

    @Override
    public void onActivate() {
        //reset wizard when activated 
        for (WizardStep<T> step : steps) {
            step.reset();
        }
        setCurrentStep(0);
    }

    protected abstract void finish();

    @Override
    protected void doLayout(float startY, float width, float height) {
        float promptHeight = VPrompt.HEIGHT;
        prompt.setBounds(0, height - promptHeight, width, promptHeight);
        height -= startY + promptHeight;
        for (WizardStep<T> step : steps) {
            step.setBounds(0, startY, width, height);
        }
    }

    public static abstract class WizardStep<T> extends FContainer {
        private final String message;
        private int index;
        private MultiStepWizardScreen<T> parentScreen;

        protected WizardStep(String message0) {
            message = message0;
        }

        public String getMessage() {
            return message;
        }

        protected void advance() {
            parentScreen.advanceStep(1);
        }

        protected abstract void reset();
        protected abstract void onActivate(T model);
        protected abstract boolean updateModelAndAdvance(T model);
    }
}
