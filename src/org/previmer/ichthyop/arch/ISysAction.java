
package org.previmer.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface ISysAction {

    public void execute(IMasterParticle particle);

    public void loadParameters() throws Exception;

    public String getParameter(String block, String key);

}
